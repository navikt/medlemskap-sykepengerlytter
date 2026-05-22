package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer

object MedlemskapVurdertKafkaConfig {

    const val TOPIC = "medlemskap-vurdert"
    const val CONSUMER_GROUP = "medlemskap-sykepengelytter-medlemskapsvurderinger"
    const val TOGGLE_NAME = "medlemskap-sykepenger-listener.medlemskap-vurdert-consumer"

    private val unleash: Unleash? = lagUnleashKlient()

    val consumerErAktivert: Boolean
        get() = unleash?.isEnabled(TOGGLE_NAME) ?: false

    private fun lagUnleashKlient(): Unleash? {
        val apiUrl = System.getenv("UNLEASH_SERVER_API_URL") ?: return null
        val apiToken = System.getenv("UNLEASH_SERVER_API_TOKEN") ?: return null
        val appName = System.getenv("NAIS_APP_NAME") ?: "medlemskap-sykepenger-listener"

        val config = UnleashConfig.builder()
            .appName(appName)
            .unleashAPI(apiUrl)
            .customHttpHeader("Authorization", apiToken)
            .build()

        return DefaultUnleash(config)
    }

    fun isEnabled(): Boolean = consumerErAktivert

    fun createConsumer(): KafkaConsumer<String, String> =
        KafkaConsumer(consumerProperties())

    private fun consumerProperties(): Map<String, Any> {
        val kafkaConfig = Configuration.KafkaConfig()
        return mapOf(
            CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to CONSUMER_GROUP,
            ConsumerConfig.CLIENT_ID_CONFIG to kafkaConfig.clientId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 100,
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to kafkaConfig.securityProtocol,
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to kafkaConfig.keystoreType,
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to kafkaConfig.keystoreLocation,
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to kafkaConfig.keystorePassword,
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to kafkaConfig.trustStorePath,
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to kafkaConfig.trustStorePassword,
        )
    }
}
