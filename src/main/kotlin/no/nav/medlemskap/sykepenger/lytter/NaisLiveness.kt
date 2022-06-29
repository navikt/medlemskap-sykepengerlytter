package no.nav.medlemskap.sykepenger.lytter

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.metrics.micrometer.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import no.nav.medlemskap.sykepenger.lytter.service.BomloService
import java.io.Writer
import java.util.*
import kotlin.collections.HashMap

fun naisLiveness(medlemskalConsumerJob:Job,bomloService: BomloService) = embeddedServer(Netty, applicationEngineEnvironment {
    connector { port = 8080 }
    module {

        install(MicrometerMetrics) {
            registry = Metrics.registry
        }

        routing {
            get("/isAlive") {
                if (medlemskalConsumerJob.isActive) {
                    call.respondText("Alive!", ContentType.Text.Plain, HttpStatusCode.OK)
                } else {
                    call.respondText("Not alive :(", ContentType.Text.Plain, HttpStatusCode.InternalServerError)
                }
            }
            get("/isReady") {
                call.respondText("Ready!", ContentType.Text.Plain, HttpStatusCode.OK)
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
            get("/metrics") {
                call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                    writeMetrics004(this, Metrics.registry)
                }
            }
        }
    }
})

suspend fun writeMetrics004(writer: Writer, registry: PrometheusMeterRegistry) {
    withContext(Dispatchers.IO) {
        kotlin.runCatching {
            TextFormat.write004(writer, registry.prometheusRegistry.metricFamilySamples())
        }
    }
}

data class Dependencies(val hashMap: MutableMap<String,Boolean>, val message:String?)
