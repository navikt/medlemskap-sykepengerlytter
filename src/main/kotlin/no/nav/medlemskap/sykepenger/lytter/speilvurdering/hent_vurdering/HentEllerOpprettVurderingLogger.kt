package no.nav.medlemskap.sykepenger.lytter.speilvurdering.hent_vurdering

import io.ktor.client.plugins.ResponseException
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest
import org.slf4j.MarkerFactory

internal class HentEllerOpprettVurderingLogger {
    private companion object {
        val log = KotlinLogging.logger { }
        val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    fun vurderingFunnet(callId: String) =
        log.info("Vurdering funnet i database for kall med id $callId")

    fun vurderingIkkeFunnet(request: SpeilvurderingRequest, callId: String) =
        log.info(
            teamLogs,
            "Ingen vurdering utført for søknad med callId: $callId. " +
                "Oppretter en ny kjøring av medlemskap-oppslag for forespørsel fra Speil",
            StructuredArguments.kv("fom", request.periode.fom),
            StructuredArguments.kv("tom", request.periode.tom),
        )

    fun feilVedSagaKall(cause: ResponseException) =
        log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
}
