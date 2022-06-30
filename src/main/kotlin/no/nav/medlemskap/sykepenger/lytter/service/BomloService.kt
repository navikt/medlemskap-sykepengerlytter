package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.features.*
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.*
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest

class BomloService(private val configuration: Configuration) {
    companion object {
        private val log = KotlinLogging.logger { }

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

    suspend fun finnVurdering(bomloRequest: BomloRequest, callId:String):JsonNode{
        try {
            val response = sagaClient.finnVurdering(bomloRequest,callId)
            log.info("Vurdering funnet i database for kall med id $callId")
            return objectMapper.readTree(response)
        }
        catch (cause: ResponseException){
            if (cause.response.status.value == 404) {
                log.warn("ingen vurdering funnet. Kaller Lovme $callId", cause)
                val lovmeRequest = mapToMedlemskapRequest(bomloRequest)
                val resultat= lovmeClient.vurderMedlemskapBomlo(lovmeRequest,callId)
                return JacksonParser().ToJson(resultat)
            }
            //TODO: Hva gjør vi med alle andre feil (400 bad request etc)
            log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
            throw cause
        }
    }

    private fun mapToMedlemskapRequest(bomloRequest: BomloRequest): MedlOppslagRequest {
        return MedlOppslagRequest(bomloRequest.fnr,bomloRequest.førsteDagForYtelse.toString(), Periode(bomloRequest.periode.fom.toString(),bomloRequest.periode.tom.toString()), Brukerinput(true))

    }
}