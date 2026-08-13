package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.sykepenger.lytter.domain.Vurderingsstatus
import org.slf4j.MarkerFactory

internal class FinnMedlemskapsstatusLogger {
    private companion object {
        val log = KotlinLogging.logger { }
        val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    fun logIngenFørstegangssøknad(request: MedlemskapsstatusRequest, callId: String) =
        log.info(
            teamLogs,
            "ingen førstegangssøknad funnet for  : ${request.fnr}, med request fom:${request.fom}, tom: ${request.tom}",
            StructuredArguments.kv("callId", callId)
        )

    fun logKallerSagaMedFørsteVurdering(
        request: MedlemskapsstatusRequest,
        førsteVurdering: Vurderingsstatus,
        callId: String
    ) =
        log.info(
            teamLogs,
            "kaller saga med første vurdering som ikke er paafolgende : fnr : ${request.fnr}, fom:${førsteVurdering.fom}, tom: ${førsteVurdering.tom}",
            StructuredArguments.kv("callId", callId)
        )

    fun logIngenMatchendeVurdering(request: MedlemskapsstatusRequest, callId: String) =
        log.info(
            teamLogs,
            "ingen matchende treff i vurderinger  funnet for  : ${request.fnr}, med request fom:${request.fom}, tom: ${request.tom}",
            StructuredArguments.kv("callId", callId)
        )

    fun logMedlemskapsstatusIkkeFunnet(request: MedlemskapsstatusRequest, callId: String) =
        log.info(
            teamLogs,
            "404 for kall mot saga på : fnr : ${request.fnr}, fom:${request.fom}, tom: ${request.tom}",
            StructuredArguments.kv("callId", callId)
        )

    fun logHttpFeil(statusCode: Int, cause: Throwable) =
        log.error("HTTP error i kall mot saga: $statusCode ", cause)
}
