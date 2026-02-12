package no.nav.medlemskap.sykepenger.lytter.clients.saga


import io.github.resilience4j.retry.Retry
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.http.runWithRetryAndMetrics
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRequest
import no.nav.medlemskap.sykepenger.lytter.rest.FlexVurderingRespons


open class SagaClient(
    private val baseUrl: String,
    private val azureAdClient: AzureAdClient,
    private val httpClient: HttpClient,
    private val retry: Retry? = null
): SagaAPI {

    override suspend fun finnVurdering(bomloRequest: BomloRequest, callId: String): String {
        val token = azureAdClient.hentTokenScopetMotMedlemskapSaga()
        return runWithRetryAndMetrics("SAGA", "vurdering", retry) {
            httpClient.post {
                url("$baseUrl/vurdering")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${token.token}")
                header("Nav-Call-Id", callId)
                header("X-Correlation-Id", callId)
                setBody(JacksonParser().ToJson(bomloRequest))
            }.body()
        }

    }

    override suspend fun finnFlexVurdering(flexRequest: FlexRequest, callId: String): String {
        val token = azureAdClient.hentTokenScopetMotMedlemskapSaga()
        return runWithRetryAndMetrics("SAGA", "flexvurdering", retry) {
            httpClient.post {
                url("$baseUrl/flexvurdering")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${token.token}")
                header("Nav-Call-Id", callId)
                header("X-Correlation-Id", callId)
                setBody(JacksonParser().ToJson(flexRequest))
            }.body()
        }
    }

    override suspend fun ping(callId: String): String {
        val token = azureAdClient.hentTokenScopetMotMedlemskapSaga()
        return runWithRetryAndMetrics("SAGA", "ping", retry) {
            httpClient.get {
                url("$baseUrl/metrics")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${token.token}")
                header("Nav-Call-Id", callId)
                header("X-Correlation-Id", callId)
            }.body()
        }

    }
}

interface SagaAPI{
    suspend fun finnVurdering(bomloRequest: BomloRequest, callId: String): String
    suspend fun finnFlexVurdering(flexRequest: FlexRequest, callId: String): String
    suspend fun ping(callId: String): String
}
