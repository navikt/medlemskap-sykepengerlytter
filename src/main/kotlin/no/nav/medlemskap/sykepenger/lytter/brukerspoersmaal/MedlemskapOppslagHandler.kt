package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import no.nav.medlemskap.sykepenger.lytter.service.ExceptionHandler

class MedlemskapOppslagHandler(requiredVariables: Map<String, String>) {

    val medlemskapOppslagRequest = medlemskapOppslagRequest(requiredVariables)

    suspend fun hentResultatFraMedlemskapOppslag(
        medlemskapOppslagService: MedlemskapOppslagService,
        callId: String
    ): String {
        return medlemskapOppslagService.kallMedlemskapOppslag(medlemskapOppslagRequest, callId)
    }

    suspend fun skalBrukerspørsmålStilles(
        medlemskapOppslagService: MedlemskapOppslagService,
        callId: String,
        exceptionHandler: ExceptionHandler,
        call: ApplicationCall,
        start: Long
    ) {

        val medlemskapOppslagResultat =
            hentResultatFraMedlemskapOppslag(medlemskapOppslagService, callId)

        when (medlemskapOppslagResultat) {
            "GradertAdresse" -> exceptionHandler.GradertAdresseException(call, callId, start)
            "TimeoutCancellationException" -> exceptionHandler.TimeoutCancellationException(
                call,
                callId,
                start,
                medlemskapOppslagRequest
            )

            else -> Respons().flexRespons(
                call,
                medlemskapOppslagResultat,
                medlemskapOppslagRequest,
                medlemskapOppslagService,
                callId
            )
        }
    }


}