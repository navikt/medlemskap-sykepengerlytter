package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.plugins.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaAPI
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import org.slf4j.MarkerFactory

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
        private val utledBrukerinput = UtledBrukerinput(GjenbrukBrukersvar(TidligereBrukersvar(persistenceService)))

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
        val brukerinput = utledBrukerinput.fraSpeilRequest(request, callId)

        val medlemskapOppslagRequest = MedlOppslagRequest(
            fnr = request.fnr,
            førsteDagForYtelse = request.førsteDagForYtelse.toString(),
            periode = Periode(request.periode.fom.toString(), request.periode.tom.toString()),
            brukerinput = brukerinput
        )

        val resultat = lovmeClient.vurderMedlemskapBomlo(medlemskapOppslagRequest, callId)
        return resultat
    }


}
