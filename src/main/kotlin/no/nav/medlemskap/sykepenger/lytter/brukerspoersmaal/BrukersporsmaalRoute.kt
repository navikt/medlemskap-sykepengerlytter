package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.security.AuthorizationHandler
import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService
import no.nav.medlemskap.sykepenger.lytter.service.Request
import org.slf4j.MarkerFactory

private val logger = KotlinLogging.logger { }
private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

fun Routing.brukerSporsmaalRoute(
    authorizationHandler: AuthorizationHandler,
    medlemskapOppslagService: MedlemskapOppslagService,
    lagFlexRespons: LagFlexRespons
) {
    authenticate("azureAuth") {
        get("/brukersporsmal") {
            val start = System.currentTimeMillis()
            val authContext = authorizationHandler.extractAuthContext(call)
            val callId = authContext.callId
            val feilhåndteringLogger = FeilhåndteringLogger()
            logger.info(
                "kall autentisert, url : /brukersporsmal",
                kv("callId", callId)
            )
            /*
            * Henter ut nødvendige parameter. map<String,String> kan evnt endres senere ved behov
            * */
            val requiredVariables = Request().hentFnrFomOgTomFraRequest(call.request)
            logger.info(
                teamLogs,
                "Forespørsel til medlemskap-oppslag: ${requiredVariables["fnr"]} , ${requiredVariables["fom"]} ,${requiredVariables["tom"]} ",
                kv("callId", callId),
                kv("endpoint", "brukersporsmal")
            )
            try {
                val medlemskapOppslagHandler = MedlemskapOppslagHandler(requiredVariables)
                when (
                    val resultat = medlemskapOppslagHandler.hentResultatFraMedlemskapOppslag(
                        callId,
                        medlemskapOppslagService
                    )
                ) {
                    MedlemskapOppslagResultat.GradertAdresse -> {
                        feilhåndteringLogger.logAdresseException(callId, start)
                        call.respond(HttpStatusCode.OK, FlexRespons(Svar.JA, emptySet()))
                    }

                    MedlemskapOppslagResultat.Tidsavbrudd -> {
                        feilhåndteringLogger.logCancellationException(callId, start, medlemskapOppslagHandler.medlemskapOppslagRequest)
                        call.respond(HttpStatusCode.InternalServerError, "Forespørsmål mot medlemskap-oppslag timet ut")
                    }

                    is MedlemskapOppslagResultat.Vurdering -> {
                        val flexRespons = lagFlexRespons.lagFlexRespons(
                            medlemskapOppslagResponse = resultat.respons,
                            medlemskapOppslagRequest = medlemskapOppslagHandler.medlemskapOppslagRequest,
                            callId = callId
                        )
                        call.respond(HttpStatusCode.OK, flexRespons)
                    }
                }
            } catch (t: Throwable) {
                call.respond(HttpStatusCode.InternalServerError, t.message!!)
            }
        }
    }
}
