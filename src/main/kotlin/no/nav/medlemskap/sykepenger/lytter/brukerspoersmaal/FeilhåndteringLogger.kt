package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import org.slf4j.MarkerFactory

class FeilhåndteringLogger {
    private val logger = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun logCancellationException(callId: String, start: Long, medlemskapOppslagRequest: MedlOppslagRequest) {
        logger.info(
            teamLogs,
            "Forespørsmål mot medlemskap-oppslag timet ut",
            StructuredArguments.kv("callId", callId),
            StructuredArguments.kv("fnr", medlemskapOppslagRequest.fnr),
            StructuredArguments.kv("tidsbrukInMs", System.currentTimeMillis() - start),
            StructuredArguments.kv("endpoint", "brukersporsmal")
        )

    }

    fun logAdresseException(callId: String, start: Long) {
        logger.info(
            teamLogs,
            "Gradert adresse",
            StructuredArguments.kv("callId", callId),
            StructuredArguments.kv("tidsbrukInMs", System.currentTimeMillis() - start),
            StructuredArguments.kv("endpoint", "brukersporsmal")
        )
    }
}