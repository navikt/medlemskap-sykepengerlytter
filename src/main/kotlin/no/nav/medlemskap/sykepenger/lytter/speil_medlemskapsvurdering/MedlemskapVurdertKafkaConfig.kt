package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer

object MedlemskapVurdertKafkaConfig {

    const val TOPIC = "medlemskap.medlemskap-vurdert"
    const val CONSUMER_GROUP = "medlemskap-sykepengelytter-medlemskapsvurderinger"
    private const val MEDLEMSKAP_VURDERT_CONSUMER = "MEDLEMSKAP_VURDERT_CONSUMER"
    private const val DEFAULT_MEDLEMSKAP_VURDERT_CONSUMER = "Nei"

    fun isEnabled(): Boolean =
        (System.getenv(MEDLEMSKAP_VURDERT_CONSUMER) ?: DEFAULT_MEDLEMSKAP_VURDERT_CONSUMER) == "Ja"

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
