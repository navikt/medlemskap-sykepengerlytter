package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.security.AuthorizationHandler
import no.nav.medlemskap.sykepenger.lytter.service.TidligereBrukersvar

private val logger = KotlinLogging.logger { }

fun Routing.testrammeverkRoute(
    authorizationHandler: AuthorizationHandler,
    tidligereBrukersvar: TidligereBrukersvar,
    erDevMiljø: Boolean = System.getenv("NAIS_CLUSTER_NAME") == "dev-gcp"
) {
    if (erDevMiljø) {
        authenticate("azureAuth") {
            post("/hentNyesteBrukersvar") {
                val authContext = authorizationHandler.extractAuthContext(call)
                val callId = authContext.callId
                logger.info(
                    "kall autentisert, url : /hentNyesteBrukersvar",
                    kv("callId", callId)
                )
                val request = call.receive<HentNyesteBrukersvarRequest>()
                val fnr = request.fnr
                if (fnr.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Felt 'fnr' mangler i body")
                    return@post
                }
                val brukerspørsmål = tidligereBrukersvar.finnNyesteBrukersvar(fnr)
                if (brukerspørsmål == null) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.OK, brukerspørsmål)
                }
            }
        }
    }
}

data class HentNyesteBrukersvarRequest(val fnr: String)
