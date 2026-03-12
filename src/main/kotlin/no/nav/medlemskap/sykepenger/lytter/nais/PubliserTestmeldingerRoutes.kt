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
import no.nav.medlemskap.sykepenger.lytter.domain.Kilde
import no.nav.medlemskap.sykepenger.lytter.service.FlexMessageHandler
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import org.slf4j.MarkerFactory
import java.time.LocalDateTime
import java.util.*

private val logger = KotlinLogging.logger { }
private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

fun Routing.publiserTestmeldinger(flexMessageHandler: FlexMessageHandler, persistenceService: PersistenceService) {

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
                        timestampType = "CREATE_TIME",
                        kilde = Kilde.LOVME_GCP
                    )

                    try {
                        logger.info(teamLogs, "Mottatt testmelding for sykepengesøknad for $callId")
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

                post("slett-brukersvar") {
                    val fnr = call.request.header("fnr")
                    if (fnr.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Mangler fnr header")
                        return@post
                    }
                    try {
                        val antallSlettet = persistenceService.slettBrukersporsmaal(fnr)
                        val antallVurderingerSlettet = persistenceService.slettVurderingsstatus(fnr)
                        logger.info(teamLogs, "Slettet $antallSlettet brukerspørsmål og $antallVurderingerSlettet vurderinger for testperson")
                        call.respond(HttpStatusCode.OK, "Slettet $antallSlettet brukerspørsmål og $antallVurderingerSlettet vurderinger")
                    } catch (t: Throwable) {
                        logger.error("Feil ved sletting av brukerspørsmål", kv("cause", t.message))
                        call.respond(HttpStatusCode.InternalServerError, t.message ?: "Ukjent feil")
                    }
                }
            }
        }

    }
}