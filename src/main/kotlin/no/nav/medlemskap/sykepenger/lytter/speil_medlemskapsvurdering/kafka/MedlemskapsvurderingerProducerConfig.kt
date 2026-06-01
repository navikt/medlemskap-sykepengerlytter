package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka

import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringSerializer

object MedlemskapsvurderingerProducerConfig {

    const val TOPIC = "medlemskap.medlemskapsvurderinger"

    fun createProducer(): KafkaProducer<String, String> =
        KafkaProducer(producerProperties())

    private fun producerProperties(): Map<String, Any> {
        val kafkaConfig = Configuration.KafkaConfig()
        return mapOf(
            CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.CLIENT_ID_CONFIG to kafkaConfig.clientId,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to kafkaConfig.securityProtocol,
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to kafkaConfig.keystoreType,
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to kafkaConfig.keystoreLocation,
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to kafkaConfig.keystorePassword,
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to kafkaConfig.trustStorePath,
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to kafkaConfig.trustStorePassword,
        )
    }
}
