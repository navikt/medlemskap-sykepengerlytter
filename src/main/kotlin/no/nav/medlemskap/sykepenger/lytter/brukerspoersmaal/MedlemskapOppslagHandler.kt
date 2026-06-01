package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.config.Configuration

class MedlemskapOppslagHandler(requiredVariables: Map<String, String>) {

    val medlemskapOppslagRequest = medlemskapOppslagRequest(requiredVariables)

    suspend fun hentResultatFraMedlemskapOppslag(
        callId: String
    ): String {
        return MedlemskapOppslagService(Configuration()).kallMedlemskapOppslag(medlemskapOppslagRequest, callId)
    }
}