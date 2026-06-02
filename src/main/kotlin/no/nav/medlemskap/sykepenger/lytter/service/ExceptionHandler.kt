package no.nav.medlemskap.sykepenger.lytter.service

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Svar
import org.slf4j.MarkerFactory

class ExceptionHandler {
    private val logger = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun TimeoutCancellationException(callId: String, start: Long, medlemskapOppslagRequest: MedlOppslagRequest) {
        logger.info(
            teamLogs,
            "Forespørsmål mot medlemskap-oppslag timet ut",
            kv("callId", callId),
            kv("fnr", medlemskapOppslagRequest.fnr),
            kv("tidsbrukInMs", System.currentTimeMillis() - start),
            kv("endpoint", "brukersporsmal")
        )

    }

    fun GradertAdresseException(callId: String, start: Long) {
        logger.info(
            teamLogs,
            "Gradert adresse",
            kv("callId", callId),
            kv("tidsbrukInMs", System.currentTimeMillis() - start),
            kv("endpoint", "brukersporsmal")
        )
    }
}