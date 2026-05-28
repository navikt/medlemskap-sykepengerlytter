package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.SpeilResponsBehandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import java.time.Duration

class MedlemskapVurdertConsumer(
    private val topic: String = MedlemskapVurdertKafkaConfig.TOPIC,
    private val kafkaEnabled: Boolean = MedlemskapVurdertKafkaConfig.isEnabled(),
    private val consumer: KafkaConsumer<String, String> = MedlemskapVurdertKafkaConfig.createConsumer(),
    private val recordHandler: SpeilResponsBehandler = SpeilResponsBehandler(
        speilResponsPublisher = if (kafkaEnabled) MedlemskapsvurderingerProducer() else SpeilResponsPublisher { }
    ),
) {

    private val log = KotlinLogging.logger { }

    init {
        if (kafkaEnabled) {
            consumer.subscribe(listOf(topic))
            log.info("MedlemskapVurdertConsumer lytter på topicet: $topic")
        } else {
            log.info("Kafka er deaktivert - MedlemskapVurdertConsumer lytter ikke på topicet: $topic")
        }
    }

    fun flow(): Flow<List<ConsumerRecord<String, String>>> = kotlinx.coroutines.flow.flow {
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
                log.info("MedlemskapVurdertConsumer mottok wakeup-signal og avslutter")
                break
            } catch (t: Throwable) {
                log.error("Feil ved polling fra $topic: ${t.message}", t)
                delay(1_000)
                emit(emptyList())
            }
        }
    }.onEach { records ->
        records.forEach { recordHandler.behandle(it) }
    }.onEach {
        if (kafkaEnabled && it.isNotEmpty()) {
            runCatching { consumer.commitSync() }
                .onFailure { e -> log.error("Commit feilet for $topic: ${e.message}") }
        }
    }
}