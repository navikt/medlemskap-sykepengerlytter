package no.nav.medlemskap.sykepenger.lytter.clients


import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagClient
import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaClient
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.flexretryRegistry
import no.nav.medlemskap.sykepenger.lytter.config.retryRegistry
import no.nav.medlemskap.sykepenger.lytter.http.cioHttpClient

class RestClients(
    private val azureAdClient: AzureAdClient,
    private val configuration: Configuration
) {
    private val flexRetry = flexretryRegistry.retry("MEDL-OPPSLAG-FLEX")
    private val medlRetry = retryRegistry.retry("MEDL-OPPSLAG")
    private val sagaRetry = retryRegistry.retry("MEDL-SAGA")

    private val httpClient = cioHttpClient
    fun medlOppslag(endpointBaseUrl: String) = MedlOppslagClient(endpointBaseUrl, azureAdClient, httpClient, medlRetry)
    fun saga(endpointBaseUrl: String) = SagaClient(endpointBaseUrl, azureAdClient, httpClient, sagaRetry)
}
