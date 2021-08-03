package no.nav.medlemskap.sykepenger.lytter.config

import com.natpryce.konfig.*
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException

private val logger = KotlinLogging.logger { }

private val defaultProperties = ConfigurationMap(
    mapOf(
        "AZURE_TENANT" to "",
        "AZURE_AUTHORITY_ENDPOINT" to "",
        "SERVICE_USER_USERNAME" to "test",
        "MEDLEMSKAP_REGLER_URL" to "",
        "MEDL2_BASE_URL" to "",
        "MEDL2_API_KEY" to "",
        "AAREG_BASE_URL" to "",
        "AAREG_API_KEY" to "",
        "SECURITY_TOKEN_SERVICE_URL" to "",
        "SECURITY_TOKEN_SERVICE_REST_URL" to "",
        "SECURITY_TOKEN_SERVICE_API_KEY" to "",
        "SERVICE_USER_PASSWORD" to "",
        "NAIS_APP_NAME" to "",
        "NAIS_CLUSTER_NAME" to "",
        "NAIS_APP_IMAGE" to "",
        "AZURE_APP_CLIENT_ID" to "",
        "SAF_BASE_URL" to "",
        "SAF_API_KEY" to "",
        "OPPGAVE_BASE_URL" to "",
        "OPPGAVE_API_KEY" to "",
        "PDL_BASE_URL" to "",
        "PDL_API_KEY" to "",
        "EREG_BASE_URL" to "",
        "EREG_API_KEY" to "",
        "UDI_BASE_URL" to "",
        "KAFKA_BROKERS" to "nav-dev-kafka-nav-dev.aivencloud.com:26484",
        "KAFKA_TRUSTSTORE_PATH" to "c:\\dev\\secrets\\client.truststore.jks",
        "KAFKA_CREDSTORE_PASSWORD" to "changeme",
        "KAFKA_KEYSTORE_PATH" to "c:\\dev\\secrets\\client.keystore.p12",
        "KAFKA_CREDSTORE_PASSWORD" to "changeme"
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
        val medl2BaseUrl: String = "MEDL2_BASE_URL".configProperty(),
        val medl2ApiKey: String = "MEDL2_API_KEY".configProperty(),
        val aaRegBaseUrl: String = "AAREG_BASE_URL".configProperty(),
        val aaRegApiKey: String = "AAREG_API_KEY".configProperty(),
        val safBaseUrl: String = "SAF_BASE_URL".configProperty(),
        val safApiKey: String = "SAF_API_KEY".configProperty(),
        val oppgaveBaseUrl: String = "OPPGAVE_BASE_URL".configProperty(),
        val oppgaveApiKey: String = "OPPGAVE_API_KEY".configProperty(),
        val pdlBaseUrl: String = "PDL_BASE_URL".configProperty(),
        val pdlApiKey: String = "PDL_API_KEY".configProperty(),
        val eregBaseUrl: String = "EREG_BASE_URL".configProperty(),
        val eregApiKey: String = "EREG_API_KEY".configProperty(),
        val udiBaseUrl: String = "UDI_BASE_URL".configProperty()
    )

    data class Sts(
        val endpointUrl: String = "SECURITY_TOKEN_SERVICE_URL".configProperty(),
        val restUrl: String = "SECURITY_TOKEN_SERVICE_REST_URL".configProperty(),
        val apiKey: String = "SECURITY_TOKEN_SERVICE_API_KEY".configProperty(),
        val username: String = "SERVICE_USER_USERNAME".configProperty(),
        val password: String = "SERVICE_USER_PASSWORD".configProperty()
    )

    data class AzureAd(
        val clientId: String = "NAIS_APP_NAME".configProperty(),
        val jwtAudience: String = "AZURE_APP_CLIENT_ID".configProperty(),
        val tenant: String = "AZURE_TENANT".configProperty(),
        val authorityEndpoint: String = "AZURE_AUTHORITY_ENDPOINT".configProperty().removeSuffix("/")
    )

    data class KafkaConfig(
        val clientId: String = "NAIS_APP_NAME".configProperty(),
        val bootstrapServers: String = "KAFKA_BROKERS".configProperty(),
        val securityProtocol: String = "SSL",
        val trustStorePath: String = "KAFKA_TRUSTSTORE_PATH".configProperty(),
        val groupID: String = "medlemskap-oppslag",
        val trustStorePassword: String = "KAFKA_CREDSTORE_PASSWORD".configProperty(),
        val keystoreType: String = "PKCS12",
        val keystoreLocation: String = "KAFKA_KEYSTORE_PATH".configProperty(),
        val keystorePassword: String = "KAFKA_CREDSTORE_PASSWORD".configProperty(),
        val topic : String =  "medlemskap.test-lovme-sykepengerlytter"
    )
}
