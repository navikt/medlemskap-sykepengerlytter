package no.nav.medlemskap.sykepenger.lytter.clients.azuread

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.http.apacheHttpClient
import no.nav.medlemskap.sykepenger.lytter.security.Token


class AzureAdClient(private val configuration: Configuration) {

    suspend fun hentTokenScopetMotMedlemskapOppslag(): Token {
        val formUrlEncode = listOf(
            "client_id" to configuration.azureAd.clientId,
            "scope" to "api://${configuration.register.medlemskapOppslagClientID}/.default",
            "client_secret" to configuration.azureAd.clientSecret,
            "grant_type" to "client_credentials"
        ).formUrlEncode()

        return apacheHttpClient.post {
            url(configuration.azureAd.tokenEndpoint)
            setBody(TextContent(formUrlEncode, ContentType.Application.FormUrlEncoded))
        }.body()
    }
    suspend fun hentTokenScopetMotMedlemskapSaga(): Token {
        val formUrlEncode = listOf(
            "client_id" to configuration.azureAd.clientId,
            "scope" to "api://${configuration.register.medlemskapSagaClientID}/.default",
            "client_secret" to configuration.azureAd.clientSecret,
            "grant_type" to "client_credentials"
        ).formUrlEncode()

        return apacheHttpClient.post {
            url(configuration.azureAd.tokenEndpoint)
            setBody(TextContent(formUrlEncode, ContentType.Application.FormUrlEncoded))
        }.body()
    }
}
