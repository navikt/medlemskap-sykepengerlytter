package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.callid.callId
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.MarkerFactory
import java.util.*

private val logger = KotlinLogging.logger { }
private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

fun Routing.medlemskapsstatusRoute(finnMedlemskapsstatus: FinnMedlemskapsstatus) {
    authenticate("azureAuth") {
        post("/flexvurdering") {
            val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
            val azp = callerPrincipal.payload.getClaim("azp").asString()
            logger.info(teamLogs, "SykepengerLytterRoutes: azp-claim i principal-token: {} ", azp)
            val callId = call.callId ?: UUID.randomUUID().toString()
            logger.info(
                "kall autentisert, url : /flexvurdering",
                kv("callId", callId),
                kv("endpoint", "flexvurdering")
            )
            val request = call.receive<MedlemskapsstatusRequest>()
            try {
                val response = finnMedlemskapsstatus.finnMedlemskapsstatus(request, callId)
                if (response != null) {
                    logger.info(
                        teamLogs,
                        "{} svar funnet for bruker {}", response.status, response.fnr,
                        kv("fnr", response.fnr),
                        kv("konklusjon", response.status),
                        kv("endpoint", "flexvurdering")
                    )
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    logger.info(
                        teamLogs,
                        "{} har ikke innslag i databasen for perioden {} - {}", request.fnr, request.fom, request.tom,
                        kv("fnr", request.fnr),
                        kv("endpoint", "flexvurdering"),
                        kv("callId", callId),
                    )
                    call.respond(HttpStatusCode.NotFound, request)
                }
            } catch (t: Throwable) {
                logger.info(
                    teamLogs,
                    "Feil ved kall mot medlemskap-oppslag",
                    kv("callId", callId),
                    kv("fnr", request.fnr),
                    kv("cause", t.stackTrace),
                    kv("endpoint", "flexvurdering")
                )
                call.respond(HttpStatusCode.InternalServerError, t.message!!)
            }
        }
    }
}
