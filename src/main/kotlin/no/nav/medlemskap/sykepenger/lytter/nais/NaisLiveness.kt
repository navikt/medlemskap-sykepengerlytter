package no.nav.medlemskap.sykepenger.lytter.nais

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.Job
import mu.KotlinLogging

import no.nav.medlemskap.sykepenger.lytter.service.BomloService
import java.util.*

fun Routing.naisRoutes(
    consumeJob: Job,bomloService: BomloService
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
    get("/status") {
        var map:MutableMap<String,Boolean> = mutableMapOf()
        try{
            bomloService.sagaClient.ping(UUID.randomUUID().toString())
            map["SAGA"] = true
            call.respondText(Dependencies(map,null).toString(), ContentType.Text.Plain, HttpStatusCode.OK)
        }
        catch (t:Exception){
            map["SAGA"] = false
            call.respondText(Dependencies(map,t.message).toString(), ContentType.Text.Plain, HttpStatusCode.ExpectationFailed)
        }

    }
}
data class Dependencies(val hashMap: MutableMap<String,Boolean>, val message:String?)
