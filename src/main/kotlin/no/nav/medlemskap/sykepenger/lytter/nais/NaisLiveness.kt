package no.nav.medlemskap.sykepenger.lytter.nais

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.Job
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.nais.writeMetrics004
import no.nav.medlemskap.sykepenger.lytter.nais.Metrics

private val logger = KotlinLogging.logger { }
private val secureLogger = KotlinLogging.logger("tjenestekall")

fun Routing.naisRoutes(
    consumeJob: Job,
) {
    get("/isAlive") {
        if (consumeJob.isActive) {
            call.respondText("Alive!", ContentType.Text.Plain, HttpStatusCode.OK)
        } else {
            call.respondText("Not alive :(", ContentType.Text.Plain, HttpStatusCode.InternalServerError)
        }
    }
    get("/isReady") {
        call.respondText("Ready!", ContentType.Text.Plain, HttpStatusCode.OK)
    }
    get("/metrics") {
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            writeMetrics004(this, Metrics.registry)
        }
    }
}
