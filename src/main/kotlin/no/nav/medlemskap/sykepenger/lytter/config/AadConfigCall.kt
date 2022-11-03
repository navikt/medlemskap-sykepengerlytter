package no.nav.medlemskap.sykepenger.lytter.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.call.*
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.http.apacheHttpClient

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureAdOpenIdConfiguration(
    @JsonProperty("jwks_uri")
    val jwksUri: String,
    @JsonProperty("issuer")
    val issuer: String,
    @JsonProperty("token_endpoint")
    val tokenEndpoint: String,
    @JsonProperty("authorization_endpoint")
    val authorizationEndpoint: String
)

private val logger = KotlinLogging.logger { }

fun getAadConfig(azureAdConfig: Configuration.AzureAd): AzureAdOpenIdConfiguration = runBlocking {
    apacheHttpClient.get("${azureAdConfig.authorityEndpoint}/${azureAdConfig.tenant}/v2.0/.well-known/openid-configuration")
        .body<AzureAdOpenIdConfiguration>().also { logger.info { it } }
}
