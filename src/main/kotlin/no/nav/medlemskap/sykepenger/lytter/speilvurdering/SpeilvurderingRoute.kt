package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.auth.jwt.JWTPrincipal
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.hent_vurdering.HentEllerOpprettVurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingMapper
import java.util.*

fun Routing.speilvurderingRoute(
    hentEllerOpprettVurdering: HentEllerOpprettVurdering,
    speilvurderingMapper: SpeilvurderingMapper,
) {
    val routeLogger = SpeilvurderingRouteLogger()

    authenticate("azureAuth") {
        post("/speilvurdering") {
            val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
            val azp = callerPrincipal.payload.getClaim("azp").asString()
            routeLogger.logAzp(azp)

            val callId = call.callId ?: UUID.randomUUID().toString()
            routeLogger.logAutentisert(callId)
            val request = call.receive<SpeilvurderingRequest>()
            routeLogger.logForespørselMottatt(request, callId)

            val response = hentEllerOpprettVurdering.finnVurdering(request, callId)
            val speilRespons = speilvurderingMapper.tilSpeilResponse(response)
            routeLogger.logVurderingFunnet(response,  callId)

            call.respond(HttpStatusCode.OK, speilRespons)
        }
    }
}
