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

    fun logIngenFørstegangssøknadFunnet(request: MedlemskapsstatusRequest, callId: String) =
        log.info(
            teamLogs,
            "Fant ikke vurderingsstatus for førstegangssøknaden tilknyttet den påfølgende søknaden for : ${request.fnr}," +
                    " med periode fom:${request.fom}, tom: ${request.tom}",
            StructuredArguments.kv("callId", callId)
        )

    fun logHenterMedlemskapsstatusForFørstegangssøknad(
        request: MedlemskapsstatusRequest,
        førsteVurdering: Vurderingsstatus,
        callId: String
    ) =
        log.info(
            teamLogs,
            "Fant vurderingsstatus for førstegangssøknaden tilknyttet den påfølgende søknaden. Henter medlemskapsstatus for " +
                    ": fnr : ${request.fnr}, fom:${førsteVurdering.fom}, tom: ${førsteVurdering.tom}",
            StructuredArguments.kv("callId", callId)
        )

    fun logFantIngenVurderingsstatus(request: MedlemskapsstatusRequest, callId: String) =
        log.info(
            teamLogs,
            "Fant ingen tidligere vurderingsstatus for : ${request.fnr}, med periode fom:${request.fom}, tom: ${request.tom}",
            StructuredArguments.kv("callId", callId)
        )

    fun logMedlemskapsstatusIkkeFunnet(request: MedlemskapsstatusRequest, callId: String) =
        log.info(
            teamLogs,
            "404 for kall mot saga på : fnr : ${request.fnr}, fom:${request.fom}, tom: ${request.tom}",
            StructuredArguments.kv("callId", callId)
        )
}
