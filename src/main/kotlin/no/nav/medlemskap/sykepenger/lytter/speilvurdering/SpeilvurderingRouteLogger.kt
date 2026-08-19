package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Speilvurdering
import org.slf4j.MarkerFactory

internal class SpeilvurderingRouteLogger {
    private companion object {
        val log = KotlinLogging.logger { }
        val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    fun logAzp(azp: String?) =
        log.info(teamLogs, "SpeilvurderingRoute: azp-claim i principal-token: {} ", azp)

    fun logAutentisert(callId: String) =
        log.info(
            "kall autentisert, url : /speilvurdering",
            kv("callId", callId),
            kv("endpoint", "speilvurdering")
        )

    fun logVurderingFunnet(
        response: Speilvurdering,
        callId: String
    ) =
        log.info(
            teamLogs,
            "{} svar funnet for bruker {}", response.speilSvar.name, response.fnr,
            kv("callId", callId),
            kv("fnr", response.fnr),
            kv("endpoint", "speilvurdering"),
            kv("soknadId", response.soknadId),
            kv("konklusjon", response.speilSvar.name),
            kv("avklaringer", response.avklaringer.toString()),
            kv("kanal", response.kanal)
        )
}
