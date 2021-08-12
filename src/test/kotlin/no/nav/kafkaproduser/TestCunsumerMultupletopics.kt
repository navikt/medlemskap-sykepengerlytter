package no.nav.kafkaproduser


import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.config.KafkaConfig
import no.nav.medlemskap.sykepenger.lytter.config.PlainStrategy
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.SaslConfigs

import org.apache.kafka.common.serialization.StringDeserializer


import java.time.Duration



import java.util.logging.Logger


fun main(args: Array<String>) {
    SimpleConsumer(KafkaConfig(System.getenv())).startConsumation()
}

class SimpleConsumer(brokers: KafkaConfig) {
    private val env: Environment = System.getenv()
    private val logger = Logger.getLogger("SimpleConsumer")
    private val securityStrategy: KafkaConfig.SecurityStrategy = PlainStrategy(System.getenv())


    fun inst2Config() = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        CommonClientConfigs.CLIENT_ID_CONFIG to Configuration.KafkaConfig().clientId,
        //ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        //KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
        //KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistry,
        ConsumerConfig.GROUP_ID_CONFIG to "multimpleSubscibersTest",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
    ) + securityStrategy.securityConfig()
    private val producer = createConsumer()
    private fun createConsumer(): org.apache.kafka.clients.consumer.Consumer<String, Object> {

        return KafkaConsumer<String, Object>(inst2Config())
    }

    fun consume(ratePerSecond: Int) {

        }
    fun startConsumation() {


        val consumer =  createConsumer()

        // Tell consumer to subscribe to topic.
        val topica = "medlemskap.test-lovme-topica"
        val topicb = "medlemskap.test-lovme-topicb"
        consumer.subscribe(listOf(topica,topicb))
        consumer.seekToBeginning(consumer.assignment())

        var noMessagesCount = 0
        val maxNoMessagesCount = 100
        while (true) {

            // Wait specified time for record(s) to arrive.
            val pollTimeout = Duration.ofSeconds(2)
            val records = consumer.poll(pollTimeout)


            // If no records received in specified time...
            if (records.count() == 0) {
                println("No messages...")
                if (++noMessagesCount > maxNoMessagesCount) {
                    break
                } else {
                    continue
                }
            }

            // Record(s) received, print them.
            System.out.printf("Poll returned %d record(s)\n", records.count())
            records.forEach {handleMessage(Event(it.topic(),it.key(),it.value())) }

            // Commit offsets returned on the last poll(), for the subscribed-to topics/partition.
            consumer.commitSync()
        }

        // Remember to close the consumer at the end, to avoid resource leakage (e.g. TCP connections).
        consumer.close()
    }

    private fun handleMessage(event: Event) {
        when (event.topic) {
            "medlemskap.test-lovme-topica" -> handleTopicA(event.key,event.value)
            "medlemskap.test-lovme-topicb" ->handleTopicB(event.key,event.value)
            else -> println("Not supported")
        }

    }
    private fun handleTopicA(key:String?,value:Object) {
        println("Business logic for topic A execurting")
        //println(value)
    }
    private fun handleTopicB(key:String?,value:Object) {
        println("Business logic for topic B execurting")
        //println(value)
    }


    data class Event (val topic:String, val key:String?, val value:Object)
}