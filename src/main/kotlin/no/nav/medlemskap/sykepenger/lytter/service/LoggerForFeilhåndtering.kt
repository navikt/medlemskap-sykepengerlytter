package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import org.slf4j.MarkerFactory

class LoggerForFeilhåndtering {
    private val logger = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun logCancellationException(callId: String, start: Long, medlemskapOppslagRequest: MedlOppslagRequest) {
        logger.info(
            teamLogs,
            "Forespørsmål mot medlemskap-oppslag timet ut",
            kv("callId", callId),
            kv("fnr", medlemskapOppslagRequest.fnr),
            kv("tidsbrukInMs", System.currentTimeMillis() - start),
            kv("endpoint", "brukersporsmal")
        )

    }

    fun logAdresseException(callId: String, start: Long) {
        logger.info(
            teamLogs,
            "Gradert adresse",
            kv("callId", callId),
            kv("tidsbrukInMs", System.currentTimeMillis() - start),
            kv("endpoint", "brukersporsmal")
        )
    }
}