package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService

class MedlemskapOppslagHandler(requiredVariables: Map<String, String>) {

    val medlemskapOppslagRequest = medlemskapOppslagRequest(requiredVariables)

    suspend fun hentResultatFraMedlemskapOppslag(
        callId: String,
        medlemskapOppslagService: MedlemskapOppslagService
    ): MedlemskapOppslagResultat =
        MedlemskapOppslagResultat.fra(
            medlemskapOppslagService.kallMedlemskapOppslag(medlemskapOppslagRequest, callId)
        )
}

sealed interface MedlemskapOppslagResultat {
    data class Vurdering(val respons: String) : MedlemskapOppslagResultat
    data object GradertAdresse : MedlemskapOppslagResultat
    data object Tidsavbrudd : MedlemskapOppslagResultat

    companion object {
        fun fra(respons: String): MedlemskapOppslagResultat =
            when (respons) {
                "GradertAdresse" -> GradertAdresse
                "TimeoutCancellationException" -> Tidsavbrudd
                else -> Vurdering(respons)
            }
    }
}