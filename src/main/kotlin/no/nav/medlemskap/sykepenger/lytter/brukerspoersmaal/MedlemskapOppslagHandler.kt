package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.config.Configuration

class MedlemskapOppslagHandler(requiredVariables: Map<String, String>) {

    val medlemskapOppslagRequest = medlemskapOppslagRequest(requiredVariables)

    suspend fun hentResultatFraMedlemskapOppslag(
        callId: String,
        medlemskapOppslagService: MedlemskapOppslagService
    ): String {
        return medlemskapOppslagService.kallMedlemskapOppslag(medlemskapOppslagRequest, callId)
    }
}