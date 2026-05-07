package no.nav.medlemskap.sykepenger.lytter.security

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callid.*
import mu.KotlinLogging
import org.slf4j.MarkerFactory
import java.util.*

private val logger = KotlinLogging.logger { }
private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

data class AuthContext(
    val azp: String,
    val callId: String
)

class AuthorizationHandler {

    fun extractAuthContext(call: ApplicationCall): AuthContext {
        val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
        val azp = callerPrincipal.payload.getClaim("azp").asString()
        logger.info(teamLogs, "SykepengerLytterRoutes: azp-claim i principal-token: {} ", azp)
        val callId = call.callId ?: UUID.randomUUID().toString()
        return AuthContext(azp = azp, callId = callId)
    }
}
