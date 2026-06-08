package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.config.FeatureToggleService
import no.nav.medlemskap.sykepenger.lytter.config.UnleashFeatureToggleService
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.SpeilResponsBehandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import java.time.Duration

class MedlemskapVurdertConsumer(
    private val topic: String = MedlemskapVurdertKafkaConfig.TOPIC,
    private val featureToggleService: FeatureToggleService = UnleashFeatureToggleService(),
    private val consumerFactory: () -> KafkaConsumer<String, String> = { MedlemskapVurdertKafkaConfig.createConsumer() },
    private val recordHandlerFactory: () -> SpeilResponsBehandler = {
        SpeilResponsBehandler(speilResponsPublisher = MedlemskapsvurderingerProducer())
    },
    private val disabledDelayMillis: Long = 5_000,
) {

    private val log = KotlinLogging.logger { }
    private var consumer: KafkaConsumer<String, String>? = null
    private var recordHandler: SpeilResponsBehandler? = null
    private var lastKafkaEnabled: Boolean? = null

    fun flow(): Flow<List<ConsumerRecord<String, String>>> = kotlinx.coroutines.flow.flow {
        while (true) {
            val kafkaEnabled = isKafkaEnabled()
            logKafkaState(kafkaEnabled)
            if (!kafkaEnabled) {
                closeConsumer()
                delay(disabledDelayMillis)
                emit(emptyList())
                continue
            }
            try {
                val records = getOrCreateConsumer().poll(Duration.ofSeconds(4)).toList()
                emit(records)
            } catch (e: WakeupException) {
                log.info("MedlemskapVurdertConsumer mottok wakeup-signal og avslutter")
                break
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                log.error("Feil ved polling fra $topic: ${t.message}", t)
                delay(1_000)
                emit(emptyList())
            }
        }
    }.onEach { records ->
        records.forEach { getOrCreateRecordHandler().behandle(it) }
    }.onEach {
        val activeConsumer = consumer
        if (activeConsumer != null && it.isNotEmpty()) {
            runCatching { activeConsumer.commitSync() }
                .onFailure { e -> log.error("Commit feilet for $topic: ${e.message}") }
        }
    }.onCompletion {
        closeConsumer()
    }

    private fun isKafkaEnabled(): Boolean =
        featureToggleService.isEnabled(MedlemskapVurdertKafkaConfig.TOGGLE_KAFKACONSUMER_AKTIV)

    private fun logKafkaState(kafkaEnabled: Boolean) {
        if (lastKafkaEnabled == kafkaEnabled) {
            return
        }
        lastKafkaEnabled = kafkaEnabled
        if (kafkaEnabled) {
            log.info("Feature toggle '${MedlemskapVurdertKafkaConfig.TOGGLE_KAFKACONSUMER_AKTIV}' er aktivert - MedlemskapVurdertConsumer vil starte")
        } else {
            log.info("Feature toggle '${MedlemskapVurdertKafkaConfig.TOGGLE_KAFKACONSUMER_AKTIV}' er ikke aktivert - MedlemskapVurdertConsumer vil ikke starte")
        }
    }

    private fun getOrCreateConsumer(): KafkaConsumer<String, String> =
        consumer ?: consumerFactory()
            .also {
                it.subscribe(listOf(topic))
                consumer = it
                log.info("MedlemskapVurdertConsumer lytter på topicet: $topic")
            }

    private fun closeConsumer() {
        consumer?.let { activeConsumer ->
            runCatching { activeConsumer.close() }
                .onSuccess { log.info("MedlemskapVurdertConsumer lytter ikke lenger på topicet: $topic") }
                .onFailure { e -> log.error("Klarte ikke å lukke MedlemskapVurdertConsumer for $topic: ${e.message}", e) }
            consumer = null
            recordHandler = null
        }
    }

    private fun getOrCreateRecordHandler(): SpeilResponsBehandler =
        recordHandler ?: recordHandlerFactory()
            .also { recordHandler = it }
}