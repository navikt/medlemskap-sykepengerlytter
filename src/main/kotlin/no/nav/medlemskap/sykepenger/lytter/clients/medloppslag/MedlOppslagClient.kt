package no.nav.medlemskap.sykepenger.lytter.clients.medloppslag


import io.github.resilience4j.retry.Retry
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.http.runWithRetryAndMetrics
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit


class MedlOppslagClient(
    private val baseUrl: String,
    private val azureAdClient: AzureAdClient,
    private val httpClient: HttpClient,
    private val retry: Retry? = null
):LovmeAPI {
    private val secureLogger = KotlinLogging.logger("tjenestekall")

    override suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        val token = azureAdClient.hentTokenScopetMotMedlemskapOppslag()
        return runWithRetryAndMetrics("MEDL-OPPSLAG", "vurdermedlemskap", retry) {
            httpClient.post {
                url("$baseUrl/kafka")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${token.token}")
                header("Nav-Call-Id", callId)
                header("X-Correlation-Id", callId)
                setBody(medlOppslagRequest)
            }.body()
        }
    }
    override suspend fun brukerspørsmål(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        val token = azureAdClient.hentTokenScopetMotMedlemskapOppslag()
        return runWithRetryAndMetrics("MEDL-OPPSLAG", "brukerspørsmål", retry) {
            try {
            withTimeout(Duration.of(20,ChronoUnit.SECONDS)){
            httpClient.post {
                url("$baseUrl/brukersporsmaal")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${token.token}")
                header("Nav-Call-Id", callId)
                header("X-Correlation-Id", callId)
                setBody(medlOppslagRequest)
            }.body()
        }
        }
            catch (t: TimeoutCancellationException){
                secureLogger.warn("Timeout i kall mot Lovme",
                    kv("fnr",medlOppslagRequest.fnr),
                    kv("callId",callId)
                )
                "TimeoutCancellationException"
            }
        }

    }
    override suspend fun vurderMedlemskapBomlo(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        val token = azureAdClient.hentTokenScopetMotMedlemskapOppslag()
        return runWithRetryAndMetrics("MEDL-OPPSLAG", "vurdermedlemskap", retry) {
            httpClient.post {
                url("$baseUrl/")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${token.token}")
                header("Nav-Call-Id", callId)
                header("X-Correlation-Id", callId)
                setBody(medlOppslagRequest)
            }.body()
        }
    }
}

interface LovmeAPI{
    suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String
    suspend fun vurderMedlemskapBomlo(medlOppslagRequest: MedlOppslagRequest, callId: String): String
    suspend fun brukerspørsmål(medlOppslagRequest: MedlOppslagRequest, callId: String): String
}
