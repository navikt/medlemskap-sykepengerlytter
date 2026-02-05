package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.plugins.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.*
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaAPI
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.*
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRequest
import no.nav.medlemskap.sykepenger.lytter.rest.FlexVurderingRespons
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import org.slf4j.MarkerFactory
import java.time.LocalDate

class BomloService(private val configuration: Configuration, var persistenceService: PersistenceService=PersistenceService(
    PostgresMedlemskapVurdertRepository(DataSourceBuilder(System.getenv()).getDataSource()) ,
    PostgresBrukersporsmaalRepository(DataSourceBuilder(System.getenv()).getDataSource())
)) {
        companion object {
            private val log = KotlinLogging.logger { }
            private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

        }

        val azureAdClient = AzureAdClient(configuration)
        val restClients = RestClients(
            azureAdClient = azureAdClient,
            configuration = configuration
        )
        var sagaClient: SagaAPI
        var lovmeClient: LovmeAPI

        private val finnForrigeBrukersvar = FinnForrigeBrukersvar(persistenceService)
        private val brukersvarGjenbruk = BrukersvarGjenbruk(finnForrigeBrukersvar)

        init {
            sagaClient = restClients.saga(configuration.register.medlemskapSagaBaseUrl)
            lovmeClient = restClients.medlOppslag(configuration.register.medlemskapOppslagBaseUrl)
        }

        suspend fun finnFlexVurdering(bomloRequest: BomloRequest, callId: String): JsonNode {
            try {
                val response = sagaClient.finnVurdering(bomloRequest, callId)
                log.info("Vurdering funnet i database for kall med id $callId")
                return objectMapper.readTree(response)
            } catch (cause: ResponseException) {
                //TODO: Avklar her om vi skal returnere 404 eller om vi må kalle Lovme!
                if (cause.response.status.value == 404) {
                    log.warn("ingen vurdering funnet. Kaller Lovme $callId", cause)
                /*    val arbeidUtland = getArbeidUtlandFromBrukerSporsmaal(bomloRequest, callId)
                    val brukersporsmaal = hentNyesteBrukerSporsmaalFromDatabase(bomloRequest, callId)
                    val lovmeRequest = mapToMedlemskapRequest(bomloRequest, arbeidUtland,brukersporsmaal)
                    val resultat = lovmeClient.vurderMedlemskapBomlo(lovmeRequest, callId)*/
                    val resultat = mapBrukersvarOgKjørRegelmotor(callId, bomloRequest)
                    return JacksonParser().ToJson(resultat)
                }
                //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
                log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
                throw cause
            }
        }

    private suspend fun mapBrukersvarOgKjørRegelmotor(callId: String, request: BomloRequest): String {
        val søknadsParametere = request.tilSøknadsParametere(callId)

        log.info(teamLogs, "Sjekker om det finnes gjenbrukbare brukersvar for forespørsel fra Speil",
            kv("fnr", søknadsParametere.fnr),
            kv("førsteDagForYtelse", søknadsParametere.førsteDagForYtelse)
        )
        val brukerinput = brukersvarGjenbruk.vurderGjenbrukAvBrukersvar(søknadsParametere)

        val medlemskapOppslagRequest = MedlOppslagRequest(
            fnr = søknadsParametere.fnr,
            førsteDagForYtelse = søknadsParametere.førsteDagForYtelse,
            periode = Periode(request.periode.fom.toString(), request.periode.tom.toString()),
            brukerinput = brukerinput
        )

        val resultat = lovmeClient.vurderMedlemskapBomlo(medlemskapOppslagRequest, callId)
        return resultat
    }

    fun BomloRequest.tilSøknadsParametere(callId: String): SoeknadsParametere =
        SoeknadsParametere(
            callId = callId,
            fnr = fnr,
            førsteDagForYtelse = førsteDagForYtelse.toString()
        )

    fun hentNyesteBrukerSporsmaalFromDatabase(bomloRequest: BomloRequest, callId: String): Brukersporsmaal {
        val listofbrukersporsmaal = persistenceService.hentbrukersporsmaalForFnr(bomloRequest.fnr)
        if (listofbrukersporsmaal.isEmpty()){
            return Brukersporsmaal(fnr = bomloRequest.fnr, soknadid = callId, eventDate = LocalDate.now(), ytelse = "SYKEPENGER", status = "IKKE_SENDT",sporsmaal = FlexBrukerSporsmaal(true))
        }

        val utfortarbeidutenfornorge = finnNyesteMedlemskap_utfort_arbeid_utenfor_norge(listofbrukersporsmaal)
        val oppholdUtenforEOS = finnNyesteMedlemskap_oppholdutenfor_eos(listofbrukersporsmaal)
        val oppholdUtenforNorge = finnNyesteMedlemskap_oppholdutenfor_norge(listofbrukersporsmaal)
        val oppholdstilatelse = finnNyesteMedlemskap_oppholdstilatelse(listofbrukersporsmaal)
        val arbeidUtlandGammelModell = finnNyesteMedlemskap_utfort_arbeid_utenfor_norgeGammelModell(listofbrukersporsmaal)

        return Brukersporsmaal(fnr = bomloRequest.fnr,
            soknadid = callId,
            eventDate = LocalDate.now(),
            ytelse = "SYKEPENGER",
            status = "SENDT",
            sporsmaal = arbeidUtlandGammelModell,
            oppholdstilatelse=oppholdstilatelse,
            utfort_arbeid_utenfor_norge = utfortarbeidutenfornorge,
            oppholdUtenforNorge = oppholdUtenforNorge,
            oppholdUtenforEOS = oppholdUtenforEOS)
    }

        suspend fun finnFlexVurdering(flexRequeest: FlexRequest, callId: String): FlexVurderingRespons? {
            val medlemskap = persistenceService.hentMedlemskap(flexRequeest.fnr)
            val found = finnMatchendeMedlemkapsPeriode(medlemskap, flexRequeest)
            //dersom vi har et innslag i vår db med status noe anent en påfølgedne, hent denne!
            if (found != null && ErMedlem.PAFOLGENDE != (found.medlem)) {
                try {
                    val response = sagaClient.finnFlexVurdering(flexRequeest, callId)
                    return JacksonParser().parseFlexVurdering(response)
                } catch (cause: ResponseException) {
                    if (cause.response.status.value == 404) {
                        log.info(
                            teamLogs,
                            "404 for kall mot saga på : fnr : ${flexRequeest.fnr}, fom:${found.fom}, tom: ${found.tom}",
                            StructuredArguments.kv("callId", callId)
                        )
                        return null
                    }
                    //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
                    log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
                    throw cause
                }

            }

            //Vi må finne første søknaden (vi støtter ikke påfølgende)
            if (found != null && ErMedlem.PAFOLGENDE == (found.medlem)) {
                val forste: Medlemskap? = finnRelevantIkkePåfølgende(found, medlemskap)
                if (forste != null) {

                    log.info(
                        teamLogs,
                        "kaller saga med første vurdering som ikke er paafolgende : fnr : ${flexRequeest.fnr}, fom:${forste.fom}, tom: ${forste.tom}",
                        StructuredArguments.kv("callId", callId)
                    )
                    try {
                        val response = sagaClient.finnFlexVurdering(
                            FlexRequest(
                                flexRequeest.sykepengesoknad_id,
                                flexRequeest.fnr,
                                forste.fom,
                                forste.tom
                            ), callId
                        )
                        return JacksonParser().parseFlexVurdering(response)
                    } catch (cause: ResponseException) {
                        if (cause.response.status.value == 404) {
                            log.info(
                                teamLogs,
                                "404 for kall mot saga på : fnr : ${flexRequeest.fnr}, fom:${forste.fom}, tom: ${forste.tom}",
                                StructuredArguments.kv("callId", callId)
                            )
                            return null
                        }
                        //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
                        log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
                        throw cause
                    }
                }
                log.info(
                    teamLogs,
                    "ingen førstegangssøknad funnet for  : ${flexRequeest.fnr}, med request fom:${flexRequeest.fom}, tom: ${flexRequeest.tom}",
                    StructuredArguments.kv("callId", callId)
                )
                return null
            }
            log.info(
                teamLogs,
                "ingen matchende treff i vurderinger  funnet for  : ${flexRequeest.fnr}, med request fom:${flexRequeest.fom}, tom: ${flexRequeest.tom}",
                StructuredArguments.kv("callId", callId)
            )
            try {
                val response = sagaClient.finnFlexVurdering(flexRequeest, callId)
                return JacksonParser().parseFlexVurdering(response)
            } catch (cause: ResponseException) {
                if (cause.response.status.value == 404) {

                    return null
                }
                //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
                log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
                throw cause
            }
        }

        suspend fun kallLovme(request: MedlOppslagRequest, callId: String): String {
            runCatching { lovmeClient.brukerspørsmål(request, callId) }
                .onFailure {
                    if (it.message?.contains("GradertAdresseException") == true) {
                        return "GradertAdresse"
                    } else {
                        throw Exception("Teknisk feil ved kall mot Lovme. Årsak : ${it.message}")
                    }
                }
                .onSuccess { return it }
            return "" //umulig å komme hit?

        }


    fun finnForrigeBrukerspørsmål(lovmeRequest: MedlOppslagRequest): List<Spørsmål> {
        return finnForrigeBrukersvar.finnForrigeStilteBrukerspørsmål(lovmeRequest.fnr, lovmeRequest.førsteDagForYtelse)
    }

        fun getArbeidUtlandFromBrukerSporsmaal(bomloRequest: BomloRequest, callId: String): Boolean {

            val brukersporsmaal = persistenceService.hentbrukersporsmaalForFnr(bomloRequest.fnr).filter {
                it.eventDate.isAfter(
                    LocalDate.now().minusYears(1)
                )
            }
            val jasvar = brukersporsmaal.filter { it.sporsmaal?.arbeidUtland == true }
            val neisvar = brukersporsmaal.filter { it.sporsmaal?.arbeidUtland == false }
            val ikkeoppgittsvar = brukersporsmaal.filter { it.sporsmaal?.arbeidUtland == null }
            //krav 2 : Er det svart JA på tidligere spørsmål, bruk denne verdien
            if (jasvar.isNotEmpty()) {
                log.info(
                    "arbeid utland ja oppgitt i tidligere søknader siste året (${jasvar.first().soknadid}) for fnr (kryptert) ${bomloRequest.fnr.sha256()}. Setter arbeid utland lik true",
                    StructuredArguments.kv("callId", callId)
                )
                return true
            }
            //krav 3 : er det svart NEI på tidligere søknader så bruk denne verdien
            if (neisvar.isNotEmpty()) {
                log.info(
                    "arbeid utland Nei oppgitt i tidligere søknader siste året (${neisvar.first().soknadid}) for fnr (kryptert) ${bomloRequest.fnr.sha256()}. Setter arbeid utland lik false",
                    StructuredArguments.kv("callId", callId)
                )
                return false
            }
            if (ikkeoppgittsvar.isEmpty()) {
                log.info("arbeid utland er ikke oppgitt  i søknad ${callId}, og heller aldri oppgitt i tidligere søknader siste året for fnr (kryptert) ${bomloRequest.fnr.sha256()}. Setter arbeid utland lik true")
                return true
            } else {
                return false
            }
        }

        private fun mapToMedlemskapRequest(bomloRequest: BomloRequest,arbeidUtland: Boolean,brukersporsmaal: Brukersporsmaal): MedlOppslagRequest {
            val brukerinput = opprettBrukerInput(brukersporsmaal,arbeidUtland)
            return MedlOppslagRequest(
                bomloRequest.fnr,
                bomloRequest.førsteDagForYtelse.toString(),
                Periode(bomloRequest.periode.fom.toString(), bomloRequest.periode.tom.toString()),
                brukerinput
            )

        }
    fun opprettBrukerInput(brukersporsmaal: Brukersporsmaal, arbeidUtland: Boolean): Brukerinput
    {
        var arbeidUtlandLocal = arbeidUtland
        val oppholdstilatelse =mapOppholdstilatelse(brukersporsmaal.oppholdstilatelse)
        val utfortAarbeidUtenforNorge= maputfortAarbeidUtenforNorge(brukersporsmaal.utfort_arbeid_utenfor_norge)
        /*
        dersom nye bruker spørsmål er oppgitt for utført arbeid utland skal disse brukes
        også på gammel modell
         */
        if (utfortAarbeidUtenforNorge != null) {
            arbeidUtlandLocal = utfortAarbeidUtenforNorge.svar
        }
        val oppholdUtenforEos = mapOppholdUtenforEOS(brukersporsmaal.oppholdUtenforEOS)
        val oppholdUtenforNorge = mapOppholdUtenforNorge(brukersporsmaal.oppholdUtenforNorge)
        return Brukerinput(
            arbeidUtenforNorge = arbeidUtlandLocal,
            oppholdstilatelse=oppholdstilatelse,
            utfortAarbeidUtenforNorge = utfortAarbeidUtenforNorge,
            oppholdUtenforEos =oppholdUtenforEos,
            oppholdUtenforNorge = oppholdUtenforNorge
        )

    }
    }

 fun finnRelevantIkkePåfølgende(paafolgende: Medlemskap, medlemskap: List<Medlemskap>): Medlemskap? {
    return medlemskap.sortedByDescending { it.tom }.find{it.tom<paafolgende.tom && it.medlem != ErMedlem.PAFOLGENDE}
}
fun finnMatchendeMedlemkapsPeriode(medlemskap: List<Medlemskap>, flexRequeest: FlexRequest): Medlemskap? {
    return medlemskap.firstOrNull {
        it.fom.isEqual(flexRequeest.fom) &&
                it.tom.isEqual(flexRequeest.tom)
    }
}