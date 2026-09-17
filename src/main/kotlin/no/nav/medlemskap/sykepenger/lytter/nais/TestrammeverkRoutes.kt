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
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadMelding
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Kilde
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadMottak
import no.nav.medlemskap.sykepenger.lytter.security.AuthorizationHandler
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import org.slf4j.MarkerFactory
import java.time.LocalDateTime
import java.util.*

private val logger = KotlinLogging.logger { }
private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

data class SlettBrukersvarRequest(val fnr: String)
data class HentNyesteBrukersvarRequest(val fnr: String)

fun Routing.testrammeverkRoutes(
    sykepengesoeknadMottak: SykepengesoeknadMottak,
    persistenceService: PersistenceService,
    authorizationHandler: AuthorizationHandler,
    testrammeverkService: TestrammeverkService,
    erDevMiljø: Boolean = System.getenv("NAIS_CLUSTER_NAME") == "dev-gcp"
) {

    if (erDevMiljø) {
        route("test") {
            authenticate("azureAuth") {
                post("publiser-sykepengesoknad") {
                    val callId = call.callId ?: UUID.randomUUID().toString()
                    val body = call.receiveText()

                    val sykepengesøknadMelding = SykepengesoeknadMelding(
                        partition = 0,
                        offset = 0,
                        value = body,
                        key = callId,
                        topic = "test.intern",
                        timestamp = LocalDateTime.now(),
                        timestampType = "CREATE_TIME",
                        kilde = Kilde.LOVME_GCP
                    )

                    logger.info(teamLogs, "Mottatt testmelding for sykepengesøknad for $callId")
                    sykepengesoeknadMottak.behandle(sykepengesøknadMelding)
                    call.respond(HttpStatusCode.OK)
                }

                post("slett-brukersvar") {
                    val request = call.receive<SlettBrukersvarRequest>()
                    val fnr = request.fnr
                    if (fnr.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Mangler fnr i request body")
                        return@post
                    }
                    val antallSlettet = persistenceService.slettBrukersporsmaal(fnr)
                    val antallVurderingerSlettet = persistenceService.slettVurderingsstatus(fnr)
                    logger.info(teamLogs, "Slettet $antallSlettet brukerspørsmål og $antallVurderingerSlettet vurderinger for testperson")
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "fnr" to fnr,
                            "slettetBrukersvar" to antallSlettet,
                            "slettetVurderingsstatuser" to antallVurderingerSlettet
                        )
                    )
                }

                post("hentNyesteBrukersvar") {
                    val authContext = authorizationHandler.extractAuthContext(call)
                    val callId = authContext.callId
                    logger.info(
                        "kall autentisert, url : /test/hentNyesteBrukersvar",
                        kv("callId", callId)
                    )
                    val request = call.receive<HentNyesteBrukersvarRequest>()
                    val fnr = request.fnr
                    if (fnr.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, "Felt 'fnr' mangler i body")
                        return@post
                    }
                    val brukerspørsmål = testrammeverkService.finnNyesteBrukersvar(fnr)
                    if (brukerspørsmål == null) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.OK, brukerspørsmål)
                    }
                }
            }
        }
    }
}