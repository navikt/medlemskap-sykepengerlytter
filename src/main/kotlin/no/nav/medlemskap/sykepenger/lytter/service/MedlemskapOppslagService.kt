package no.nav.medlemskap.sykepenger.lytter.service

import kotlinx.coroutines.CancellationException
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
        return try {
            medlemskapOppslagClient.brukerspørsmål(request, callId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e.message?.contains("GradertAdresseException") == true) {
                "GradertAdresse"
            } else {
                throw IllegalStateException("Teknisk feil ved kall mot Lovme", e)
            }
        }
    }

    suspend fun vurderMedlemskap(request: MedlemskapOppslagRequest, callId: String): String {
        return medlemskapOppslagClient.vurderMedlemskap(request, callId)
    }
}
