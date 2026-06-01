package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal


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
        start: Long
    ): String {

        val medlemskapOppslagResultat =
            hentResultatFraMedlemskapOppslag(medlemskapOppslagService, callId)

        when (medlemskapOppslagResultat) {
            "GradertAdresse" -> {
                exceptionHandler.GradertAdresseException(callId, start)
                return "GradertAdresse"
            }

            "TimeoutCancellationException" -> {
                exceptionHandler.TimeoutCancellationException(
                    callId,
                    start,
                    medlemskapOppslagRequest
                )
                return "TimeoutCancellationException"
            }

            else -> return Respons().lagFlexRespons(
                medlemskapOppslagResultat,
                medlemskapOppslagRequest,
                medlemskapOppslagService,
                callId
            )
        }
    }


}