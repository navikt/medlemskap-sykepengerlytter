package no.nav.medlemskap.sykepenger.lytter.nais

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.domain.lagMedlemskapsResultat
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar
import no.nav.medlemskap.sykepenger.lytter.service.BomloService
import no.nav.medlemskap.sykepenger.lytter.service.RegelMotorResponsHandler
import java.lang.NullPointerException
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
                val response = bomloService.finnVurdering(request, callId)
                val medlemskapsloggObjekt = response.lagMedlemskapsResultat()

                secureLogger.info(
                    "{} konklusjon gitt for bruker {}", medlemskapsloggObjekt.svar, medlemskapsloggObjekt.fnr,
                    kv("fnr", medlemskapsloggObjekt.fnr),
                    kv("svar", medlemskapsloggObjekt.svar),
                    kv("årsak", medlemskapsloggObjekt.årsak),
                    kv("årsaker", medlemskapsloggObjekt.årsaker)
                )

                call.respond(HttpStatusCode.OK, response)
            } catch (t: Throwable) {
                secureLogger.error(
                    "Unexpected error calling Lovme",
                    kv("callId", callId),
                    kv("fnr", request.fnr),
                    kv("cause", t.stackTrace)
                )
                call.respond(HttpStatusCode.InternalServerError, t.message!!)
            }
        }
        get("/brukersporsmal") {
            val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
            val azp = callerPrincipal.payload.getClaim("azp").asString()
            secureLogger.info("EvalueringRoute: azp-claim i principal-token: {} ", azp)
            val callId = call.callId ?: UUID.randomUUID().toString()
            logger.info(
                "kall autentisert, url : /brukerspørsmål",
                kv("callId", callId)
            )
           val  requiredVariables:Map<String,String> = getRequiredVariables(call.request)

            secureLogger.info ("Calling Lovme  : ${requiredVariables["fnr"]} , ${requiredVariables["fom"]} ,${requiredVariables["tom"]} ",
                kv("callId", callId)
            )
            try {
                val lovmeRequest = MedlOppslagRequest(
                    fnr=requiredVariables["fnr"]!!,
                    førsteDagForYtelse = requiredVariables["fom"]!!,
                    periode = Periode(
                        fom=requiredVariables["fom"]!!,
                        tom = requiredVariables["tom"]!!),
                    brukerinput = Brukerinput(false))
                val lovmeresponse = bomloService.kallLovme(lovmeRequest,callId)
                if (lovmeresponse=="GradertAdresse"){
                    call.respond(HttpStatusCode.BadRequest,"bruker har gradert adresse")
                }
                else{
                    val flexRespons= RegelMotorResponsHandler().interpretLovmeRespons(lovmeresponse)
                    call.respond(HttpStatusCode.OK,flexRespons)
                }
                call.respond(HttpStatusCode.Conflict,"ukjent tilstand i tjeneste. Kontakt utvikler!")
            }
            catch (t:Throwable){
                call.respond(HttpStatusCode.InternalServerError,t)
            }

        }
    }


}

 fun getRequiredVariables(request: ApplicationRequest): Map<String, String> {
    val headers = setOf("fnr")
    val queryParams = setOf("fom","tom")
    var returnMap = mutableMapOf<String,String>()
    for (variabel in headers ){
        try {
            returnMap[variabel] = request.headers[variabel]!!
        }
        catch (e:NullPointerException){
        throw BadRequestException("Header '$variabel' mangler")
        }
    }
    for (variabel in queryParams ){
        try {
            returnMap[variabel] = request.queryParameters[variabel]!!
        }
        catch (v:NullPointerException){
            throw BadRequestException("QueryParameter '$variabel' mangler")
        }
    }

    return returnMap
}



