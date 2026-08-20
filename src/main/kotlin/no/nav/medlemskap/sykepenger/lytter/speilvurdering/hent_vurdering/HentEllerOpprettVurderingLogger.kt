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

    fun vurderingFunnet(request: SpeilvurderingRequest, callId: String) =
        log.info(teamLogs, "Fant vurderingen for fnr ${request.fnr} for $callId i medlemskap-saga")

    fun vurderingIkkeFunnet(request: SpeilvurderingRequest, callId: String) =
        log.info(
            teamLogs,
            "Ingen vurdering er utført for søknaden med callId: $callId. " +
                "Oppretter en ny kjøring av medlemskap-oppslag for forespørsel fra Speil for fnr: ${request.fnr}",
            StructuredArguments.kv("fom", request.periode.fom),
            StructuredArguments.kv("tom", request.periode.tom),
        )

    fun feilVedSagaKall(cause: ResponseException) =
        log.error("Teknisk feil mot medlemskap-saga: ${cause.response.status.value} ", cause)
}
