package no.nav.medlemskap.sykepenger.lytter.nais

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.domain.FlexMessageRecord
import no.nav.medlemskap.sykepenger.lytter.service.FlexMessageHandler
import java.time.LocalDateTime
import java.util.*

private val logger = KotlinLogging.logger { }

fun Routing.publiserTestmeldinger(flexMessageHandler: FlexMessageHandler) {

    val cluster = System.getenv("NAIS_CLUSTER_NAME")

    if (cluster == "dev-gcp") {
        route("test") {
            authenticate("azureAuth") {
                post("publiser-sykepengesoknad") {
                    val callId = call.callId ?: UUID.randomUUID().toString()
                    val body = call.receiveText()

                    val flexMessageRecord = FlexMessageRecord(
                        partition = 0,
                        offset = 0,
                        value = body,
                        key = callId,
                        topic = "test.intern",
                        timestamp = LocalDateTime.now(),
                        timestampType = "CREATE_TIME"
                    )

                    try {
                        flexMessageHandler.handle(flexMessageRecord)
                        call.respond(HttpStatusCode.OK)
                    } catch (t: Throwable) {
                        logger.error(
                            "Feil ved behandling av testmelding",
                            kv("callId", callId),
                            kv("cause", t.message)
                        )
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            t.message ?: "Ukjent feil"
                        )
                    }
                }
            }
        }

    }
}