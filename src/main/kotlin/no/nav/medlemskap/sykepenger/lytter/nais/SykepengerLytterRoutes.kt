package no.nav.medlemskap.sykepenger.lytter.nais

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import no.nav.medlemskap.sykepenger.lytter.service.BomloService
import java.util.*

private val logger = KotlinLogging.logger { }
private val secureLogger = KotlinLogging.logger("tjenestekall")
fun Routing.sykepengerLytterRoutes(bomloService: BomloService) {
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
                val request = call.receive<BomloRequest>()
                try {
                    val response = bomloService.finnVurdering(request,callId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (t: Throwable) {
                    secureLogger.error("Unexpected error calling Lovmme",
                    kv("callId", callId),
                    kv("fnr",request.fnr),
                    kv("cause",t.stackTrace)
                    )
                    call.respond(HttpStatusCode.InternalServerError,t.message!!)
                }
            }

        }
}
