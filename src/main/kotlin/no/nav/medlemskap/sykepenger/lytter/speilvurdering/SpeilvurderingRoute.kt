package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.auth.jwt.JWTPrincipal
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.MarkerFactory
import java.util.*

private val logger = KotlinLogging.logger { }
private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

fun Routing.speilvurderingRoute(
    finnVurderingForSpeil: FinnVurderingForSpeil,
) {
    authenticate("azureAuth") {
        post("/speilvurdering") {
            val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
            val azp = callerPrincipal.payload.getClaim("azp").asString()
            logger.info(teamLogs, "SpeilvurderingRoute: azp-claim i principal-token: {} ", azp)
            val callId = call.callId ?: UUID.randomUUID().toString()
            logger.info(
                "kall autentisert, url : /speilvurdering",
                kv("callId", callId),
                kv("endpoint", "speilvurdering")
            )
            val start = System.currentTimeMillis()
            val request = call.receive<SpeilvurderingRequest>()
            try {
                val response = finnVurderingForSpeil.finnVurdering(request, callId)
                val speilRespons = SpeilvurderingMapper().tilSpeilResponse(response)
                val timeInMS = System.currentTimeMillis() - start
                logger.info(
                    teamLogs,
                    "{} svar funnet for bruker {}", speilRespons.speilSvar.name, speilRespons.fnr,
                    kv("callId", callId),
                    kv("fnr", request.fnr),
                    kv("tidsbrukInMs", timeInMS),
                    kv("endpoint", "speilvurdering"),
                    kv("soknadId", speilRespons.soknadId),
                    kv("konklusjon", speilRespons.speilSvar.name),
                    kv("avklaringer", response.avklaringer.toString()),
                    kv("kanal", response.kanal)
                )

                call.respond(HttpStatusCode.OK, speilRespons)
            } catch (t: Throwable) {
                val timeInMS = System.currentTimeMillis() - start
                logger.info(
                    teamLogs,
                    "Feil ved kall mot medlemskap-oppslag",
                    kv("callId", callId),
                    kv("fnr", request.fnr),
                    kv("cause", t.stackTrace),
                    kv("tidsbrukInMs", timeInMS),
                    kv("endpoint", "speilvurdering")
                )
                call.respond(HttpStatusCode.InternalServerError, t.message!!)
            }
        }
    }
}
