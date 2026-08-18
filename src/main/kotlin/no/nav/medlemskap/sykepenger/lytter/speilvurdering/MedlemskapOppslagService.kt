package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlemskapOppslagAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapOppslagVurdering
import no.nav.medlemskap.sykepenger.lytter.config.Configuration

class MedlemskapOppslagService(private val medlemskapOppslagApi: MedlemskapOppslagAPI) {

    constructor(configuration: Configuration) : this(
        RestClients(
            azureAdClient = AzureAdClient(configuration),
            configuration = configuration
        ).medlOppslag(configuration.register.medlemskapOppslagBaseUrl)
    )

    suspend fun vurderMedlemskapForSpeil(request: MedlOppslagRequest, callId: String): MedlemskapOppslagVurdering =
        medlemskapOppslagApi.vurderMedlemskapForSpeil(request, callId)
}
