package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import io.ktor.client.plugins.ResponseException
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import org.slf4j.MarkerFactory

internal class BomloServiceLogger {
    private companion object {
        val log = KotlinLogging.logger { }
        val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    fun vurderingFunnet(callId: String) =
        log.info("Vurdering funnet i database for kall med id $callId")

    fun vurderingIkkeFunnet(request: BomloRequest, callId: String) =
        log.info(
            teamLogs,
            "Ingen vurdering utført for søknad med callId: $callId. " +
                "Oppretter en ny kjøring av medlemskap-oppslag for forespørsel fra Speil",
            StructuredArguments.kv("fnr", request.fnr),
            StructuredArguments.kv("fom", request.periode.fom),
            StructuredArguments.kv("tom", request.periode.tom),
        )

    fun lovmeKalles(callId: String, cause: ResponseException) =
        log.warn("ingen vurdering funnet. Kaller Lovme $callId", cause)

    fun feilVedSagaKall(cause: ResponseException) =
        log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
}
