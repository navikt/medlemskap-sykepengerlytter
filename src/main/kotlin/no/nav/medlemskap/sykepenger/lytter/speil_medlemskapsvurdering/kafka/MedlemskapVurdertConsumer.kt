package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.config.FeatureToggleService
import no.nav.medlemskap.sykepenger.lytter.feature_toggles.FeatureToggles
import no.nav.medlemskap.sykepenger.lytter.feature_toggles.UnleashFeatureToggleService
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.SpeilResponsBehandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import java.time.Duration

class MedlemskapVurdertConsumer(
    private val topic: String = MedlemskapVurdertConsumerConfig.TOPIC,
    private val featureToggleService: FeatureToggleService = UnleashFeatureToggleService(),
    private val consumerFactory: () -> KafkaConsumer<String, String> = { MedlemskapVurdertConsumerConfig.createConsumer() },
    private val recordHandlerFactory: () -> SpeilResponsBehandler = {
        SpeilResponsBehandler(speilResponsPublisher = MedlemskapsvurderingerProducer())
    },
    private val disabledDelayMillis: Long = 5_000,
) {

    private val log = KotlinLogging.logger { }
    private var consumer: KafkaConsumer<String, String>? = null
    private var recordHandler: SpeilResponsBehandler? = null
    private var sistLoggetConsumerStatus: Boolean? = null

    fun flow(): Flow<List<ConsumerRecord<String, String>>> = flow {
        while (true) {
            if (!erConsumerAktiv()) {
                ventMensConsumerErDeaktivert()
                emit(emptyList())
                continue
            }
            try {
                emit(hentMeldingerFraConsumer())
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
    }.onEach { meldinger ->
        behandleMeldinger(meldinger)
    }.onEach { meldinger ->
        commitHvisMeldingerBleBehandlet(meldinger)
    }.onCompletion {
        lukkConsumerOgRecordHandler()
    }

    private fun erConsumerAktiv(): Boolean =
        erConsumerToggleAktiv().also { loggConsumerStatusHvisEndret(it) }

    private fun erConsumerToggleAktiv(): Boolean =
        featureToggleService.isEnabled(FeatureToggles.MEDLEMSKAP_VURDERT_CONSUMER_AKTIV)

    private suspend fun ventMensConsumerErDeaktivert() {
        lukkConsumerOgRecordHandler()
        delay(disabledDelayMillis)
    }

    private fun hentMeldingerFraConsumer(): List<ConsumerRecord<String, String>> =
        hentEllerOpprettConsumer().poll(Duration.ofSeconds(4)).toList()

    private fun behandleMeldinger(meldinger: List<ConsumerRecord<String, String>>) {
        meldinger.forEach { hentEllerOpprettRecordHandler().behandle(it) }
    }

    private fun commitHvisMeldingerBleBehandlet(meldinger: List<ConsumerRecord<String, String>>) {
        val aktivConsumer = consumer
        if (aktivConsumer != null && meldinger.isNotEmpty()) {
            runCatching { aktivConsumer.commitSync() }
                .onFailure { e -> log.error("Commit feilet for $topic: ${e.message}") }
        }
    }

    private fun loggConsumerStatusHvisEndret(consumerAktiv: Boolean) {
        if (sistLoggetConsumerStatus == consumerAktiv) {
            return
        }
        sistLoggetConsumerStatus = consumerAktiv
        if (consumerAktiv) {
            log.info("Feature toggle '${FeatureToggles.MEDLEMSKAP_VURDERT_CONSUMER_AKTIV}' er aktivert - MedlemskapVurdertConsumer vil starte")
        } else {
            log.info("Feature toggle '${FeatureToggles.MEDLEMSKAP_VURDERT_CONSUMER_AKTIV}' er ikke aktivert - MedlemskapVurdertConsumer vil ikke starte")
        }
    }

    private fun hentEllerOpprettConsumer(): KafkaConsumer<String, String> =
        consumer ?: consumerFactory()
            .also {
                it.subscribe(listOf(topic))
                consumer = it
                log.info("MedlemskapVurdertConsumer lytter på topicet: $topic")
            }

    private fun lukkConsumerOgRecordHandler() {
        consumer?.let { aktivConsumer ->
            runCatching { aktivConsumer.close() }
                .onSuccess { log.info("MedlemskapVurdertConsumer lytter ikke lenger på topicet: $topic") }
                .onFailure { e -> log.error("Klarte ikke å lukke MedlemskapVurdertConsumer for $topic: ${e.message}", e) }
            consumer = null
            recordHandler = null
        }
    }

    private fun hentEllerOpprettRecordHandler(): SpeilResponsBehandler =
        recordHandler ?: recordHandlerFactory()
            .also { recordHandler = it }
}