package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.callid.callId
import java.util.UUID

fun Routing.medlemskapsstatusRoute(finnMedlemskapsstatus: FinnMedlemskapsstatus) {
    val routeLogger = MedlemskapsstatusRouteLogger()

    authenticate("azureAuth") {
        post("/flexvurdering") {
            val callId = call.callId ?: UUID.randomUUID().toString()
            routeLogger.logAutentisert(callId)

            val request = call.receive<MedlemskapsstatusRequest>()
            val response = finnMedlemskapsstatus.finnMedlemskapsstatus(request, callId)

            if (response == null) {
                routeLogger.logMedlemskapsstatusIkkeFunnet(request, callId)
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            routeLogger.logMedlemskapsstatusFunnet(response, callId)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
