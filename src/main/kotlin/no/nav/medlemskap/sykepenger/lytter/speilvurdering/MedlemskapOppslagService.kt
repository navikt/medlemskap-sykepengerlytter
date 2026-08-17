package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.config.Configuration

class MedlemskapOppslagService(private val lovmeApi: LovmeAPI) {

    constructor(configuration: Configuration) : this(
        RestClients(
            azureAdClient = AzureAdClient(configuration),
            configuration = configuration
        ).medlOppslag(configuration.register.medlemskapOppslagBaseUrl)
    )

    suspend fun vurderMedlemskap(request: MedlOppslagRequest, callId: String): String =
        lovmeApi.vurderMedlemskapBomlo(request, callId)
}
