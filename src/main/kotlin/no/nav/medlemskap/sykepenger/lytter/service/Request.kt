package no.nav.medlemskap.sykepenger.lytter.service

import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.ApplicationRequest
import java.lang.NullPointerException

class Request {
    fun hentFnrFomOgTomFraRequest(request: ApplicationRequest): Map<String, String> {
        var returnMap = mutableMapOf<String,String>()
        val headers = setOf("fnr")
        for (variabel in headers ){
            try {
                returnMap[variabel] = request.headers[variabel]!!
            }
            catch (e: java.lang.NullPointerException){
                throw BadRequestException("Header '$variabel' mangler")
            }
        }
        val queryParams = setOf("fom","tom")
        for (variabel in queryParams ){
            try {
                returnMap[variabel] = request.queryParameters[variabel]!!
            }
            catch (v: NullPointerException){
                throw BadRequestException("QueryParameter '$variabel' mangler")
            }
        }
        return returnMap
    }

}