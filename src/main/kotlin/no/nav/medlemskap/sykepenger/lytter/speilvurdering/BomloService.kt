package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.plugins.ResponseException
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import no.nav.medlemskap.sykepenger.lytter.service.UtledBrukerinput
import org.slf4j.MarkerFactory

class BomloService(
    private val sagaService: SagaService,
    private val medlemskapOppslagService: MedlemskapOppslagService,
    private val utledBrukerinput: UtledBrukerinput
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    //Brukt av speilvurdering-endepunktet
    suspend fun finnFlexVurdering(bomloRequest: BomloRequest, callId: String): JsonNode {
        try {
            val medlemskapsvurdering = sagaService.finnVurdering(bomloRequest, callId)
            log.info("Vurdering funnet i database for kall med id $callId")
            return objectMapper.readTree(medlemskapsvurdering.json)
        } catch (cause: ResponseException) {
            //TODO: Avklar her om vi skal returnere 404 eller om vi må kalle Lovme!
            if (cause.response.status.value == 404) {
                log.info(teamLogs, "Ingen vurdering utført for søknad med callId: ${callId}. " +
                        "Oppretter en ny kjøring av medlemskap-oppslag for forespørsel fra Speil",
                    StructuredArguments.kv("fnr", bomloRequest.fnr),
                    StructuredArguments.kv("fom", bomloRequest.periode.fom),
                    StructuredArguments.kv("tom", bomloRequest.periode.tom),
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

    suspend fun pingSaga(callId: String): String =
        sagaService.ping(callId)

    //Brukt av speilvurdering-endepunktet (når det ikke finnes en eksisterende vurdering i databasen)
    private suspend fun mapBrukersvarOgKjørRegelmotor(callId: String, request: BomloRequest): String {
        val brukerinput = utledBrukerinput.fraSpeilRequest(request, callId)

        val medlemskapOppslagRequest = MedlOppslagRequest(
            fnr = request.fnr,
            førsteDagForYtelse = request.førsteDagForYtelse.toString(),
            periode = Periode(request.periode.fom.toString(), request.periode.tom.toString()),
            brukerinput = brukerinput
        )

        val resultat = medlemskapOppslagService.vurderMedlemskap(medlemskapOppslagRequest, callId)
        return resultat
    }


}