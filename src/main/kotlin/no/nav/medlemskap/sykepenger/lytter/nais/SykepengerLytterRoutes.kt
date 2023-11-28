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
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.rest.*
import no.nav.medlemskap.sykepenger.lytter.service.BomloService
import no.nav.medlemskap.sykepenger.lytter.service.RegelMotorResponsHandler
import no.nav.medlemskap.sykepenger.lytter.service.createFlexRespons
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
                kv("callId", callId),
                kv("endpoint", "vurdering")
            )
            val request = call.receive<BomloRequest>()
            try {
                val response = bomloService.finnFlexVurdering(request, callId)
                val medlemskapsloggObjekt = response.lagMedlemskapsResultat()

                secureLogger.info(
                    "{} konklusjon gitt for bruker {}", medlemskapsloggObjekt.svar, medlemskapsloggObjekt.fnr,
                    kv("fnr", medlemskapsloggObjekt.fnr),
                    kv("svar", medlemskapsloggObjekt.svar),
                    kv("årsak", medlemskapsloggObjekt.årsak),
                    kv("årsaker", medlemskapsloggObjekt.årsaker),
                    kv("response",response.toPrettyString()),
                    kv("endpoint", "vurdering")
                )

                call.respond(HttpStatusCode.OK, response.toPrettyString())
            } catch (t: Throwable) {
                secureLogger.error(
                    "Unexpected error calling Lovme",
                    kv("callId", callId),
                    kv("fnr", request.fnr),
                    kv("cause", t.stackTrace),
                    kv("endpoint", "vurdering")
                )
                call.respond(HttpStatusCode.InternalServerError, t.message!!)
            }
        }
        post("/speilvurdering") {
            val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
            val azp = callerPrincipal.payload.getClaim("azp").asString()
            secureLogger.info("EvalueringRoute: azp-claim i principal-token: {} ", azp)
            val callId = call.callId ?: UUID.randomUUID().toString()
            logger.info(
                "kall autentisert, url : /speilvurdering",
                kv("callId", callId),
                kv("endpoint", "speilvurdering")
            )
            val request = call.receive<BomloRequest>()
            try {
                val response = bomloService.finnFlexVurdering(request, callId)
                val speilRespons = response.lagSpeilRespons(callId)

                call.respond(HttpStatusCode.OK, speilRespons)
            } catch (t: Throwable) {
                secureLogger.error(
                    "Unexpected error calling Lovme",
                    kv("callId", callId),
                    kv("fnr", request.fnr),
                    kv("cause", t.stackTrace),
                    kv("endpoint", "speilvurdering")
                )
                call.respond(HttpStatusCode.InternalServerError, t.message!!)
            }
        }
        post("/flexvurdering") {
            val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
            val azp = callerPrincipal.payload.getClaim("azp").asString()
            secureLogger.info("EvalueringRoute: azp-claim i principal-token: {} ", azp)
            val callId = call.callId ?: UUID.randomUUID().toString()
            logger.info(
                "kall autentisert, url : /vurdering",
                kv("callId", callId),
                kv("endpoint", "flexvurdering")
            )
            val request = call.receive<FlexRequest>()
            try {
                val response = bomloService.finnFlexVurdering(request, callId)
                if (response!=null){
                    secureLogger.info(
                        "{} svar funnet for flex for bruker {}", response.status, response.fnr,
                        kv("fnr", response.fnr),
                        kv("svar", response.status),
                        kv("endpoint", "flexvurdering")
                    )
                    call.respond(HttpStatusCode.OK, response)
                }
                else{
                    secureLogger.warn("{} har ikke innslag i databasen for perioden {} - {}", request.fnr, request.fom,request.tom,
                        kv("fnr", request.fnr),
                        kv("endpoint", "flexvurdering"),
                        kv("callId", callId),
                    )
                    call.respond(HttpStatusCode.NotFound,request)
                }
            } catch (t: Throwable) {
                secureLogger.error(
                    "Unexpected error calling Lovme",
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
            val callerPrincipal: JWTPrincipal = call.authentication.principal()!!
            val azp = callerPrincipal.payload.getClaim("azp").asString()
            secureLogger.info("EvalueringRoute: azp-claim i principal-token: {} ", azp)
            val callId = call.callId ?: UUID.randomUUID().toString()
            logger.info(
                "kall autentisert, url : /brukerspørsmål",
                kv("callId", callId)
            )
           /*
           * Henter ut nødvendige parameter. map<String,String> kan evnt endres senere ved behov
           * */
            val  requiredVariables:Map<String,String> = getRequiredVariables(call.request)

            secureLogger.info ("Calling Lovme  : ${requiredVariables["fnr"]} , ${requiredVariables["fom"]} ,${requiredVariables["tom"]} ",
                kv("callId", callId),
                kv("endpoint", "brukersporsmal")
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
                    secureLogger.warn("Kall fra flex på gradert adresse",
                        kv("callId", callId),
                        kv("fnr", lovmeRequest.fnr),
                        kv("tidsbrukInMs",System.currentTimeMillis()-start),
                        kv("endpoint", "brukersporsmal")
                    )
                    call.respond(HttpStatusCode.OK,FlexRespons(Svar.JA, emptySet()))
                }
                if (lovmeresponse=="TimeoutCancellationException"){
                    secureLogger.warn("Kall mot Lovme Timet ut",
                        kv("callId", callId),
                        kv("fnr", lovmeRequest.fnr),
                        kv("tidsbrukInMs",System.currentTimeMillis()-start),
                        kv("endpoint", "brukersporsmal")
                    )
                    call.respond(HttpStatusCode.InternalServerError,"Kall mot Lovme timed ut")
                }
                else{

                    val foreslaattRespons = RegelMotorResponsHandler().interpretLovmeRespons(lovmeresponse)
                    val alleredeStilteSporsmaal = bomloService.hentAlleredeStilteBrukerSpørsmål(lovmeRequest.fnr)
                    val flexRespons:FlexRespons =  createFlexRespons(foreslaattRespons,alleredeStilteSporsmaal)

                    secureLogger.info("Svarer brukerspørsmål",
                        kv("callId", callId),
                        kv("fnr", lovmeRequest.fnr),
                        kv("brukersporsmal", JacksonParser().ToJson(flexRespons.sporsmal).toPrettyString()),
                        kv("tidsbrukInMs",System.currentTimeMillis()-start),
                        kv("endpoint", "brukersporsmal"),
                        kv("eksiterende_sporsmaal",JacksonParser().ToJson(alleredeStilteSporsmaal).toPrettyString())
                    )
                    call.respond(HttpStatusCode.OK,flexRespons)
                }
                call.respond(HttpStatusCode.InternalServerError,"ukjent tilstand i tjeneste. Kontakt utvikler!")
            }
            catch (t:Throwable){
                call.respond(HttpStatusCode.InternalServerError,t.message!!)
            }
        }
    }
}



fun getRequiredVariables(request: ApplicationRequest): Map<String, String> {
    var returnMap = mutableMapOf<String,String>()
     val headers = setOf("fnr")
     for (variabel in headers ){
        try {
            returnMap[variabel] = request.headers[variabel]!!
        }
        catch (e:NullPointerException){
            throw BadRequestException("Header '$variabel' mangler")
        }
    }
     val queryParams = setOf("fom","tom")
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



