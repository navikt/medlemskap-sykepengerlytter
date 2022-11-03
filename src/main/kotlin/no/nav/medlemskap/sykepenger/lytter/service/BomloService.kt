package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.plugins.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.saga.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.*
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import java.time.LocalDate

class BomloService(private val configuration: Configuration) {
    companion object {
        private val log = KotlinLogging.logger { }

    }
    val persistenceService: PersistenceService = PersistenceService(
        PostgresMedlemskapVurdertRepository(DataSourceBuilder(System.getenv()).getDataSource()) ,
        PostgresBrukersporsmaalRepository(DataSourceBuilder(System.getenv()).getDataSource())
    )
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

    suspend fun finnVurdering(bomloRequest: BomloRequest, callId:String):JsonNode{
        try {
            val response = sagaClient.finnVurdering(bomloRequest,callId)
            log.info("Vurdering funnet i database for kall med id $callId")
            return objectMapper.readTree(response)
        }
        catch (cause: ResponseException){
            if (cause.response.status.value == 404) {
                log.warn("ingen vurdering funnet. Kaller Lovme $callId", cause)
                val arbeidUtland = getArbeidUtlandFromBrukerSporsmaal(bomloRequest, callId)
                val lovmeRequest = mapToMedlemskapRequest(bomloRequest,arbeidUtland)
                val resultat= lovmeClient.vurderMedlemskapBomlo(lovmeRequest,callId)
                return JacksonParser().ToJson(resultat)
            }
            //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
            log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
            throw cause
        }
    }
    fun getArbeidUtlandFromBrukerSporsmaal(bomloRequest: BomloRequest,callId: String): Boolean {

        val brukersporsmaal = persistenceService.hentbrukersporsmaalForFnr(bomloRequest.fnr).filter { it.eventDate.isAfter(
            LocalDate.now().minusYears(1)) }
        val jasvar =  brukersporsmaal.filter { it.sporsmaal?.arbeidUtland ==true }
        val neisvar =  brukersporsmaal.filter { it.sporsmaal?.arbeidUtland ==false }
        val ikkeoppgittsvar = brukersporsmaal.filter { it.sporsmaal?.arbeidUtland ==null }
        //krav 2 : Er det svart JA på tidligere spørsmål, bruk denne verdien
        if (jasvar.isNotEmpty()) {
            log.info("arbeid utland ja oppgitt i tidligere søknader siste året (${jasvar.first().soknadid}) for fnr (kryptert) ${bomloRequest.fnr.sha256()}. Setter arbeid utland lik true",
                StructuredArguments.kv("callId", callId)
            )
            return true
        }
        //krav 3 : er det svart NEI på tidligere søknader så bruk denne verdien
        if (neisvar.isNotEmpty()){
            log.info("arbeid utland Nei oppgitt i tidligere søknader siste året (${neisvar.first().soknadid}) for fnr (kryptert) ${bomloRequest.fnr.sha256()}. Setter arbeid utland lik false",
                StructuredArguments.kv("callId", callId)
            )
            return false
        }
        if (ikkeoppgittsvar.isEmpty()){
            log.info("arbeid utland er ikke oppgitt  i søknad ${callId}, og heller aldri oppgitt i tidligere søknader siste året for fnr (kryptert) ${bomloRequest.fnr.sha256()}. Setter arbeid utland lik true")
            return true
        }

        else{
            return false
        }
    }

    private fun mapToMedlemskapRequest(bomloRequest: BomloRequest,arbeidUtland:Boolean): MedlOppslagRequest {
        return MedlOppslagRequest(bomloRequest.fnr,bomloRequest.førsteDagForYtelse.toString(), Periode(bomloRequest.periode.fom.toString(),bomloRequest.periode.tom.toString()), Brukerinput(arbeidUtland))

    }
}