package no.nav.medlemskap.sykepenger.lytter.clients.medloppslag


import io.github.resilience4j.retry.Retry
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.saga.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.http.runWithRetryAndMetrics


class MedlOppslagClient(
    private val baseUrl: String,
    private val azureAdClient: AzureAdClient,
    private val httpClient: HttpClient,
    private val retry: Retry? = null
):LovmeAPI {

    override suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        val token = azureAdClient.hentTokenScopetMotMedlemskapOppslag()
        return runWithRetryAndMetrics("MEDL-OPPSLAG", "vurdermedlemskap", retry) {
            httpClient.post {
                url("$baseUrl/kafka")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${token.token}")
                header("Nav-Call-Id", callId)
                header("X-Correlation-Id", callId)
                body = medlOppslagRequest
            }
        }
    }
}

interface LovmeAPI{
    suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String
}
