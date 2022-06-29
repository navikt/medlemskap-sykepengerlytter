package no.nav.medlemskap.sykepenger.lytter.nais

import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.respond
import io.ktor.routing.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import java.util.*

private val logger = KotlinLogging.logger { }
private val secureLogger = KotlinLogging.logger("tjenestekall")
fun Routing.sykepengerLytterRoutes() {
    route("/vurdering") {
        authenticate("azureAuth") {
            post("/vurdering") {
                val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
                val azp = callerPrincipal.payload.getClaim("azp").asString()
                secureLogger.info("EvalueringRoute: azp-claim i principal-token: {} ", azp)
                val callId = call.callId ?: UUID.randomUUID().toString()
                logger.info(
                    "kall autentisert, url : /vurdering",
                    kv("callId", callId)
                )

                try {
                    val request = call.receive<MedlOppslagRequest>()
                    call.respond(HttpStatusCode.OK, "Kall er OK ")
                } catch (t: Throwable) {
                    call.respond(t.stackTrace)
                }
            }

        }
    }
}
