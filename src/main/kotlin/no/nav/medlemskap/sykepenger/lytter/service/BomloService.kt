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

    //Brukt av speilvurdering-endepunktet
    suspend fun finnFlexVurdering(bomloRequest: BomloRequest, callId: String): JsonNode {
        try {
            val response = sagaClient.finnVurdering(bomloRequest, callId)
            log.info("Vurdering funnet i database for kall med id $callId")
            return objectMapper.readTree(response)
        } catch (cause: ResponseException) {
            //TODO: Avklar her om vi skal returnere 404 eller om vi må kalle Lovme!
            if (cause.response.status.value == 404) {
                log.info(teamLogs, "Ingen vurdering utført for søknad med callId: ${callId}. " +
                        "Oppretter en ny kjøring av medlemskap-oppslag for forespørsel fra Speil",
                    kv("fnr", bomloRequest.fnr),
                    kv("fom", bomloRequest.periode.fom),
                    kv("tom", bomloRequest.periode.tom),
                )
                log.warn("ingen vurdering funnet. Kaller Lovme $callId", cause)
                val resultat = mapBrukersvarOgKjørRegelmotor(callId, bomloRequest)
                return JacksonParser().ToJson(resultat)
            }
            //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
            log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
            throw cause
        }
    }

    //Brukt av speilvurdering-endepunktet (når det ikke finnes en eksisterende vurdering i databasen)
    private suspend fun mapBrukersvarOgKjørRegelmotor(callId: String, request: BomloRequest): String {
        val søknadsParametere = request.tilSøknadsParametere(callId)

        log.info(
            teamLogs, "Sjekker om det finnes gjenbrukbare brukersvar for forespørsel fra Speil",
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


    //Brukt av flexvurdering-endepunktet
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

    //Brukt av brukersporsmal-endepunktet
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

    //Brukt av brukersporsmal-endepunktet
    fun finnForrigeBrukerspørsmål(lovmeRequest: MedlOppslagRequest): List<Spørsmål> {
        return finnForrigeBrukersvar.finnForrigeStilteBrukerspørsmål(lovmeRequest.fnr, lovmeRequest.førsteDagForYtelse)
    }

    private fun BomloRequest.tilSøknadsParametere(callId: String): SoeknadsParametere =
        SoeknadsParametere(
            callId = callId,
            fnr = fnr,
            førsteDagForYtelse = førsteDagForYtelse.toString()
        )
}


fun finnRelevantIkkePåfølgende(paafolgende: Medlemskap, medlemskap: List<Medlemskap>): Medlemskap? {
    return medlemskap.sortedByDescending { it.tom }
        .find { it.tom < paafolgende.tom && it.medlem != ErMedlem.PAFOLGENDE }
}

fun finnMatchendeMedlemkapsPeriode(medlemskap: List<Medlemskap>, flexRequeest: FlexRequest): Medlemskap? {
    return medlemskap.firstOrNull {
        it.fom.isEqual(flexRequeest.fom) &&
                it.tom.isEqual(flexRequeest.tom)
    }
}
