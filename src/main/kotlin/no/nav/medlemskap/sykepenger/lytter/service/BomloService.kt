package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.plugins.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.saga.persistence.Brukersporsmaal
import no.nav.medlemskap.saga.persistence.Medlemskap_utfort_arbeid_utenfor_norge
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.*
import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaAPI
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRequest
import no.nav.medlemskap.sykepenger.lytter.rest.FlexVurderingRespons
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import java.time.LocalDate

class BomloService(private val configuration: Configuration, var persistenceService: PersistenceService=PersistenceService(
    PostgresMedlemskapVurdertRepository(DataSourceBuilder(System.getenv()).getDataSource()) ,
    PostgresBrukersporsmaalRepository(DataSourceBuilder(System.getenv()).getDataSource())
)) {
        companion object {
            private val log = KotlinLogging.logger { }
            private val secureLogger = KotlinLogging.logger("tjenestekall")

        }

        val azureAdClient = AzureAdClient(configuration)
        val restClients = RestClients(
            azureAdClient = azureAdClient,
            configuration = configuration
        )
        var sagaClient: SagaAPI
        var lovmeClient: LovmeAPI


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
                if (cause.response.status.value == 404) {
                    log.warn("ingen vurdering funnet. Kaller Lovme $callId", cause)
                    val arbeidUtland = getArbeidUtlandFromBrukerSporsmaal(bomloRequest, callId)
                    val brukersporsmaal: Brukersporsmaal? = getBrukerSporsmaal(bomloRequest, callId)

                    val lovmeRequest = mapToMedlemskapRequest(bomloRequest, arbeidUtland)
                    val resultat = lovmeClient.vurderMedlemskapBomlo(lovmeRequest, callId)
                    return JacksonParser().ToJson(resultat)
                }
                //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
                log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
                throw cause
            }
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
                        secureLogger.info(
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
                val forste: Medlemskap? = finnRelevantIkkePåfølgende(found!!, medlemskap)
                if (forste != null) {

                    secureLogger.info(
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
                            secureLogger.info(
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
                secureLogger.info(
                    "ingen førstegangssøknad funnet for  : ${flexRequeest.fnr}, med request fom:${flexRequeest.fom}, tom: ${flexRequeest.tom}",
                    StructuredArguments.kv("callId", callId)
                )
                return null
            }
            secureLogger.info(
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

        //TODO: Logikken under må avklares så vi kan forholde oss til ny modell
        fun getBrukerSporsmaal(bomloRequest: BomloRequest, callId: String): Brukersporsmaal? {

            val brukersporsmaal = persistenceService.hentbrukersporsmaalForFnr(bomloRequest.fnr).filter {
                it.eventDate.isAfter(
                    LocalDate.now().minusYears(1)
                )
            }
            val arbeidUtenForNorge: Medlemskap_utfort_arbeid_utenfor_norge? =
                UtfortArbeidUtenForNorge(brukersporsmaal, callId)
            val arbeidUtlandGammelModell: Boolean =
                arbeidUtenForNorgeGammelModell(brukersporsmaal, callId, bomloRequest)
            return null
        }

        fun hentAlleredeStilteBrukerSpørsmål(fnr: String): List<Spørsmål> {

            val alleBrukerSpormaalForBruker = persistenceService.hentbrukersporsmaalForFnr(fnr).filter {
                it.eventDate.isAfter(
                    LocalDate.now().minusYears(1)
                )
            }
            val alleredespurteBrukersporsmaal: List<Spørsmål> =
                finnAlleredeStilteBrukerSprøsmål(alleBrukerSpormaalForBruker)
            return alleredespurteBrukersporsmaal
        }


        private fun arbeidUtenForNorgeGammelModell(
            brukersporsmaal: List<Brukersporsmaal>,
            callId: String,
            bomloRequest: BomloRequest
        ): Boolean {
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

        private fun UtfortArbeidUtenForNorge(
            brukersporsmaal: List<Brukersporsmaal>,
            callId: String
        ): Medlemskap_utfort_arbeid_utenfor_norge? {
            val jasvar = brukersporsmaal.filter { it.utfort_arbeid_utenfor_norge?.svar == true }
            val neisvar = brukersporsmaal.filter { it.utfort_arbeid_utenfor_norge?.svar == false }
            val ikkeoppgittsvar = brukersporsmaal.filter { it.utfort_arbeid_utenfor_norge == null }
            //krav 2 : Er det svart JA på tidligere spørsmål, bruk denne verdien
            if (jasvar.isNotEmpty()) {
                log.info(
                    "arbeid utland ja oppgitt i tidligere søknader siste året (${jasvar.first().soknadid}) for fnr (kryptert) ${jasvar.first().fnr}. Setter arbeid utland lik true",
                    StructuredArguments.kv("callId", callId)
                )
                return jasvar.first().utfort_arbeid_utenfor_norge
            }
            //krav 3 : er det svart NEI på tidligere søknader så bruk denne verdien
            if (neisvar.isNotEmpty()) {
                log.info(
                    "arbeid utland Nei oppgitt i tidligere søknader siste året (${neisvar.first().soknadid}) for fnr (kryptert) ${neisvar.first().fnr}. Setter arbeid utland lik false",
                    StructuredArguments.kv("callId", callId)
                )
                return neisvar.first().utfort_arbeid_utenfor_norge
            }
            if (ikkeoppgittsvar.isEmpty()) {
                log.info("arbeid utland er ikke oppgitt  i søknad ${callId}, og heller aldri oppgitt i tidligere søknader siste året for fnr (kryptert) ${ikkeoppgittsvar.first().fnr}. Setter arbeid utland lik true")
                return null
            } else {
                return null
            }
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

        private fun mapToMedlemskapRequest(bomloRequest: BomloRequest, arbeidUtland: Boolean): MedlOppslagRequest {
            return MedlOppslagRequest(
                bomloRequest.fnr,
                bomloRequest.førsteDagForYtelse.toString(),
                Periode(bomloRequest.periode.fom.toString(), bomloRequest.periode.tom.toString()),
                Brukerinput(arbeidUtland)
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