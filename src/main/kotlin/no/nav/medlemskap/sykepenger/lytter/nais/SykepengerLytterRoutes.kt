package no.nav.medlemskap.sykepenger.lytter.nais

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
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.MedlemskapOppslagHandler
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.MedlemskapOppslagService
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.Respons
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.medlemskapOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.rest.*
import no.nav.medlemskap.sykepenger.lytter.security.AuthorizationHandler
import no.nav.medlemskap.sykepenger.lytter.service.BomloService
import no.nav.medlemskap.sykepenger.lytter.service.ExceptionHandler
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.service.Request
import org.slf4j.MarkerFactory
import java.util.*

private val logger = KotlinLogging.logger { }
private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

fun Routing.sykepengerLytterRoutes(
    bomloService: BomloService,
    medlemskapOppslagService: MedlemskapOppslagService,
    authorizationHandler: AuthorizationHandler,
    persistenceService: PersistenceService
) {
    authenticate("azureAuth") {
        post("/speilvurdering") {
            val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
            val azp = callerPrincipal.payload.getClaim("azp").asString()
            logger.info(teamLogs, "SykepengerLytterRoutes: azp-claim i principal-token: {} ", azp)
            val callId = call.callId ?: UUID.randomUUID().toString()
            logger.info(
                "kall autentisert, url : /speilvurdering",
                kv("callId", callId),
                kv("endpoint", "speilvurdering")
            )
            val start = System.currentTimeMillis()
            val request = call.receive<BomloRequest>()
            try {
                val response = bomloService.finnFlexVurdering(request, callId)
                val speilRespons = response.lagSpeilRespons(callId)
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
                    kv("avklaringer", response.hentAvklaringer().toString()),
                    kv("kanal", response.hentKanal())
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
            val request = call.receive<FlexRequest>()
            try {
                val response = bomloService.finnFlexVurdering(request, callId)
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

        get("/brukersporsmal") {
            val start = System.currentTimeMillis()
            val authContext = authorizationHandler.extractAuthContext(call)
            val callId = authContext.callId
            val exceptionHandler = ExceptionHandler()
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
                val skalBrukerspørsmålStilles = MedlemskapOppslagHandler(requiredVariables).skalBrukerspørsmålStilles(
                    medlemskapOppslagService,
                    callId,
                    exceptionHandler,
                    start
                )
                when (skalBrukerspørsmålStilles) {
                    "GradertAdresse" -> {
                        call.respond(HttpStatusCode.OK, FlexRespons(Svar.JA, emptySet()))
                    }

                    "TimeoutCancellationException" -> {
                        call.respond(HttpStatusCode.InternalServerError, "Forespørsmål mot medlemskap-oppslag timet ut")
                    }

                    else -> {
                        call.respond(HttpStatusCode.OK, skalBrukerspørsmålStilles)
                    }
                }
            } catch (t: Throwable) {
                call.respond(HttpStatusCode.InternalServerError, t.message!!)
            }
        }
    }
}
