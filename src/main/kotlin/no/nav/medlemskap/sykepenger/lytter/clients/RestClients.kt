package no.nav.medlemskap.sykepenger.lytter.clients


import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagClient
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_saga.MedlemskapMedlemskapSagaClient
import no.nav.medlemskap.sykepenger.lytter.config.retryRegistry
import no.nav.medlemskap.sykepenger.lytter.http.cioHttpClient

class RestClients(
    private val azureAdClient: AzureAdClient
) {
    private val medlRetry = retryRegistry.retry("MEDL-OPPSLAG")
    private val sagaRetry = retryRegistry.retry("MEDL-SAGA")

    private val httpClient = cioHttpClient
    fun medlOppslag(endpointBaseUrl: String) = MedlemskapOppslagClient(endpointBaseUrl, azureAdClient, httpClient, medlRetry)
    fun saga(endpointBaseUrl: String) = MedlemskapMedlemskapSagaClient(endpointBaseUrl, azureAdClient, httpClient, sagaRetry)
}
