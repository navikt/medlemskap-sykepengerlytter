package no.nav.medlemskap.sykepenger.lytter.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

open class KafkaConfig(
    environment: Environment,
    private val securityStrategy: SecurityStrategy = PlainStrategy(environment = environment)
) {

    val topic = Configuration.KafkaConfig().topic
    val medlemskapVurdertTopic = Configuration.KafkaConfig().medlemskapVurdertTopic
    val enabled = Configuration.KafkaConfig().enabled


    fun inst2Config() = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        CommonClientConfigs.CLIENT_ID_CONFIG to Configuration.KafkaConfig().clientId,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.GROUP_ID_CONFIG to Configuration.KafkaConfig().groupID,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 10,

    ) + securityStrategy.securityConfig()

    fun inst2MedlemskapVurdertConfig() = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        CommonClientConfigs.CLIENT_ID_CONFIG to Configuration.KafkaConfig().clientId,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.GROUP_ID_CONFIG to Configuration.KafkaConfig().medlemskapVurdertConsumerGroupID,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 10,

        ) + securityStrategy.securityConfig()

    fun createConsumer() = KafkaConsumer<String, String>(inst2Config())

    fun createMedlemskapVurdertConsumer() = KafkaConsumer<String, String>(inst2MedlemskapVurdertConfig())

    interface SecurityStrategy {
        fun securityConfig(): Map<String, String>
    }
}