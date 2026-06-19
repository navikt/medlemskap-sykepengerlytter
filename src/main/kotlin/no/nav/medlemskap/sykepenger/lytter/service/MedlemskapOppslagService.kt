package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.config.Configuration

class MedlemskapOppslagService(private val medlemskapOppslagClient: LovmeAPI) {

    constructor(configuration: Configuration) : this(
        RestClients(
            azureAdClient = AzureAdClient(configuration),
            configuration = configuration
        ).medlOppslag(configuration.register.medlemskapOppslagBaseUrl)
    )

    suspend fun kallMedlemskapOppslag(request: MedlOppslagRequest, callId: String): String {
        runCatching { medlemskapOppslagClient.brukerspørsmål(request, callId) }
            .onFailure {
                if (it.message?.contains("GradertAdresseException") == true) {
                    return "GradertAdresse"
                } else {
                    throw Exception("Teknisk feil ved kall mot Lovme. Årsak : ${it.message}")
                }
            }
            .onSuccess { return it }
        return "" //umulig å komme hit?

    }

    suspend fun vurderMedlemskap(request: MedlOppslagRequest, callId: String): String {
        return medlemskapOppslagClient.vurderMedlemskap(request, callId)
    }
}
