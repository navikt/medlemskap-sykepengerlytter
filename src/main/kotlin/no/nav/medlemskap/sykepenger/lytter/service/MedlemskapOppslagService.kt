package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.config.Configuration

class MedlemskapOppslagService(private val medlemskapOppslagClient: MedlemskapOppslagAPI) {

    constructor(configuration: Configuration) : this(
        RestClients(
            azureAdClient = AzureAdClient(configuration)
        ).medlOppslag(configuration.register.medlemskapOppslagBaseUrl)
    )

    suspend fun kallMedlemskapOppslag(request: MedlemskapOppslagRequest, callId: String): String {
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

    suspend fun vurderMedlemskap(request: MedlemskapOppslagRequest, callId: String): String {
        return medlemskapOppslagClient.vurderMedlemskap(request, callId)
    }
}
