package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.ContentTransformationException
import kotlinx.coroutines.CancellationException
import java.util.UUID

fun Routing.medlemskapsstatusRoute(finnMedlemskapsstatus: FinnMedlemskapsstatus) {
    val routeLogger = MedlemskapsstatusRouteLogger()

    authenticate("azureAuth") {
        post("/flexvurdering") {
            val callId = call.callId ?: UUID.randomUUID().toString()
            routeLogger.logAutentisert(callId)

            try {
                val request = call.receive<MedlemskapsstatusRequest>()
                val response = finnMedlemskapsstatus.finnMedlemskapsstatus(request, callId)

                if (response != null) {
                    routeLogger.logMedlemskapsstatusFunnet(response, callId)
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    routeLogger.logMedlemskapsstatusIkkeFunnet(request, callId)
                    call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ContentTransformationException) {
                routeLogger.logUgyldigRequest(e, callId)
                call.respond(HttpStatusCode.BadRequest)
            } catch (e: Exception) {
                routeLogger.logFeil(e, callId)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
