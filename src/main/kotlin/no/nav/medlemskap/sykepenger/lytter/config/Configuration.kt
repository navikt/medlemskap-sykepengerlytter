package no.nav.medlemskap.sykepenger.lytter.config

import com.natpryce.konfig.*
import jdk.jfr.Enabled
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException

private val logger = KotlinLogging.logger { }

private val defaultProperties = ConfigurationMap(
    mapOf(
        "AZURE_OPENID_CONFIG_TOKEN_ENDPOINT" to "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/oauth2/v2.0/token",
        "AZURE_APP_WELL_KNOWN_URL" to "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/v2.0/.well-known/openid-configuration",
        "AZURE_TENANT" to "966ac572-f5b7-4bbe-aa88-c76419c0f851",
        "AZURE_AUTHORITY_ENDPOINT" to "https://login.microsoftonline.com",
        "SERVICE_USER_USERNAME" to "test",
        "SECURITY_TOKEN_SERVICE_URL" to "https://api-gw-q1.oera.no/sts/SecurityTokenServiceProvider/",
        "SECURITY_TOKEN_SERVICE_REST_URL" to "https://api-gw-q1.oera.no/security-token-service",
        "SECURITY_TOKEN_SERVICE_API_KEY" to "",
        "SERVICE_USER_PASSWORD" to "",
        "NAIS_APP_NAME" to "",
        "NAIS_CLUSTER_NAME" to "",
        "NAIS_APP_IMAGE" to "",
        "AZURE_APP_CLIENT_ID" to "ee472fd1-3621-4600-a6ac-69d3662e993f",
        "AZURE_APP_CLIENT_SECRET" to "IAyNwh.W9h780hjE_Xdf-a6Dw0DP-Vf2Tm    ",
        "MEDL_OPPSLAG_API_KEY" to "",
        "MEDL_OPPSLAG_BASE_URL" to "https://medlemskap-oppslag.dev.intern.nav.no",
        "MEDL_OPPSLAG_CLIENT_ID" to "2719da58-489e-4185-9ee6-74b7e93763d2",
        "KAFKA_BROKERS" to "nav-dev-kafka-nav-dev.aivencloud.com:26484",
        "KAFKA_TRUSTSTORE_PATH" to "c:\\dev\\secrets\\client.truststore.jks",
        "KAFKA_CREDSTORE_PASSWORD" to "changeme",
        "KAFKA_KEYSTORE_PATH" to "c:\\dev\\secrets\\client.keystore.p12",
        "KAFKA_CREDSTORE_PASSWORD" to "changeme",
        "KAFKA_ENABLED" to "false",
        "BRUKERSPORSMAAL_ENABLED" to "Ja"
    )
)

private val config = ConfigurationProperties.systemProperties() overriding
    EnvironmentVariables overriding
    defaultProperties

private fun String.configProperty(): String = config[Key(this, stringType)]

private fun String.readFile() =
    try {
        logger.info { "Leser fra azure-fil $this" }
        File(this).readText(Charsets.UTF_8)
    } catch (err: FileNotFoundException) {
        logger.warn { "Azure fil ikke funnet" }
        null
    }

private fun hentCommitSha(image: String): String {
    val parts = image.split(":")
    if (parts.size == 1) return image
    return parts[1].substring(0, 7)
}

data class Configuration(
    val register: Register = Register(),
    val sts: Sts = Sts(),
    val azureAd: AzureAd = AzureAd(),
    val kafkaConfig: KafkaConfig = KafkaConfig(),
    val cluster: String = "NAIS_CLUSTER_NAME".configProperty(),
    val commitSha: String = hentCommitSha("NAIS_APP_IMAGE".configProperty())
) {
    data class Register(
        val medlemskapOppslagBaseUrl: String = "MEDL_OPPSLAG_BASE_URL".configProperty(),
        val medlemskapOppslagClientID: String = "MEDL_OPPSLAG_CLIENT_ID".configProperty(),
        val medlemskapSagaBaseUrl: String = "MEDL_SAGA_BASE_URL".configProperty(),
        val medlemskapSagaClientID: String = "MEDL_SAGA_CLIENT_ID".configProperty(),
    )

    data class Sts(
        val endpointUrl: String = "SECURITY_TOKEN_SERVICE_URL".configProperty(),
        val restUrl: String = "SECURITY_TOKEN_SERVICE_REST_URL".configProperty(),
        val apiKey: String = "SECURITY_TOKEN_SERVICE_API_KEY".configProperty(),
        val username: String = "SERVICE_USER_USERNAME".configProperty(),
        val password: String = "SERVICE_USER_PASSWORD".configProperty()
    )

    data class AzureAd(
        val clientId: String = "AZURE_APP_CLIENT_ID".configProperty(),
        val clientSecret: String = "AZURE_APP_CLIENT_SECRET".configProperty(),
        val jwtAudience: String = "AZURE_APP_CLIENT_ID".configProperty(),
        val tokenEndpoint: String = "AZURE_OPENID_CONFIG_TOKEN_ENDPOINT".configProperty().removeSuffix("/"),
        val azureAppWellKnownUrl: String = "AZURE_APP_WELL_KNOWN_URL".configProperty().removeSuffix("/")
    )


    data class KafkaConfig(
        val clientId: String = "NAIS_APP_NAME".configProperty(),
        val bootstrapServers: String = "KAFKA_BROKERS".configProperty(),
        val securityProtocol: String = "SSL",
        val trustStorePath: String = "KAFKA_TRUSTSTORE_PATH".configProperty(),
        val groupID: String = "medlemskap-sykepenger-listener_v2",
        val flexConsumerGroup: String = "medlemskap-sykepenger-flex-listener",
        val trustStorePassword: String = "KAFKA_CREDSTORE_PASSWORD".configProperty(),
        val keystoreType: String = "PKCS12",
        val keystoreLocation: String = "KAFKA_KEYSTORE_PATH".configProperty(),
        val keystorePassword: String = "KAFKA_CREDSTORE_PASSWORD".configProperty(),
        val enabled: String = "KAFKA_ENABLED".configProperty(),
        val brukersporsmaal_enabled: String = "BRUKERSPORSMAAL_ENABLED".configProperty(),
        val topic : String =  "flex.sykepengesoknad-lovme-filter",
        val flextopic : String =  "flex.sykepengesoknad"
    )
}
