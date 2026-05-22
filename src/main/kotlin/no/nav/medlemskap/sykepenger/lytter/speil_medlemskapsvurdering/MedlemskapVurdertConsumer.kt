package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import java.time.Duration

class MedlemskapVurdertConsumer(
    private val topic: String = MedlemskapVurdertKafkaConfig.TOPIC,
    private val kafkaEnabled: Boolean = MedlemskapVurdertKafkaConfig.isEnabled(),
    private val consumer: KafkaConsumer<String, String> = MedlemskapVurdertKafkaConfig.createConsumer()
) {

    private val log = KotlinLogging.logger { }

    init {
        if (kafkaEnabled) {
            consumer.subscribe(listOf(topic))
            log.info("MedlemskapVurdertConsumer subscribed to topic $topic")
        } else {
            log.info("Kafka is disabled - MedlemskapVurdertConsumer will not subscribe to $topic")
        }
    }

    fun flow(): Flow<List<ConsumerRecord<String, String>>> = flow {
        while (true) {
            if (!kafkaEnabled) {
                delay(5_000)
                emit(emptyList())
                continue
            }
            try {
                val records = consumer.poll(Duration.ofSeconds(4)).toList()
                emit(records)
            } catch (e: WakeupException) {
                log.info("MedlemskapVurdertConsumer woken up - shutting down")
                break
            } catch (t: Throwable) {
                log.error("Feil ved polling fra $topic: ${t.message}", t)
                delay(1_000)
                emit(emptyList())
            }
        }
    }.onEach { records ->
        records.forEach { handle(it) }
    }.onEach {
        if (kafkaEnabled && it.isNotEmpty()) {
            runCatching { consumer.commitSync() }
                .onFailure { e -> log.error("Commit feilet for $topic: ${e.message}") }
        }
    }

    private fun handle(record: ConsumerRecord<String, String>) {
        log.info(
            "Mottatt melding fra $topic",
            kv("callId", record.key()),
            kv("topic", record.topic()),
            kv("partition", record.partition()),
            kv("offset", record.offset()),
        )
    }
}
