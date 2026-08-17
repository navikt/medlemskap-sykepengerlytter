package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.MarkerFactory

internal class MedlemskapsstatusRouteLogger {
    private companion object {
        val log = KotlinLogging.logger { }
        val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    fun logAutentisert(callId: String) =
        log.info(
            "kall autentisert, url : /flexvurdering",
            kv("callId", callId),
            kv("endpoint", "flexvurdering")
        )

    fun logMedlemskapsstatusFunnet(response: Medlemskapsstatus, callId: String) =
        log.info(
            teamLogs,
            "{} svar funnet for bruker {}", response.status, response.fnr,
            kv("callId", callId),
            kv("fnr", response.fnr),
            kv("konklusjon", response.status),
            kv("endpoint", "flexvurdering")
        )

    fun logMedlemskapsstatusIkkeFunnet(request: MedlemskapsstatusRequest, callId: String) =
        log.info(
            teamLogs,
            "{} har ikke innslag i databasen for perioden {} - {}", request.fnr, request.fom, request.tom,
            kv("fnr", request.fnr),
            kv("endpoint", "flexvurdering"),
            kv("callId", callId),
        )

    fun logUgyldigRequest(exception: Exception, callId: String) =
        log.warn(exception) {
            "Ugyldig request til medlemskapsstatus, callId=$callId, endpoint=flexvurdering"
        }

    fun logFeil(exception: Exception, callId: String) =
        log.error(exception) {
            "Feil ved kall mot medlemskap-oppslag, callId=$callId, endpoint=flexvurdering"
        }
}
