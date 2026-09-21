package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.kafka

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.nais.Metrics
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadMottak
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadMelding
import org.apache.kafka.clients.consumer.CommitFailedException
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean

class SykepengesoeknadConsumer(
    private val config: SykepengesoeknadKafkaConfig,
    private val service: SykepengesoeknadMottak,
    private val consumer: KafkaConsumer<String, String>,
    ) {

    private val logger = KotlinLogging.logger { }
    private val running = AtomicBoolean(true)

    init {
        consumer.subscribe(listOf(config.flexTopic))
    }

    fun pollMessages(): List<SykepengesoeknadMelding> =

        consumer.poll(Duration.ofSeconds(4))
            .map {
                SykepengesoeknadMelding(
                    partition = it.partition(),
                    offset = it.offset(),
                    value = it.value(),
                    key = it.key(),
                    topic = it.topic(),
                    timestamp = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(it!!.timestamp()), ZoneId.systemDefault()
                    ),
                    timestampType = it.timestampType().name
                )
            }
            .also {
                Metrics.incReceivedvurderingTotal(it.count())
            }

    fun flow(): Flow<List<SykepengesoeknadMelding>> =
        kotlinx.coroutines.flow.flow {
            while (running.get()) {

                if (config.brukersporsmaal_enabled != "Ja") {
                    logger.debug("Kafka is disabled. Does not fetch messages from topic")
                    emit(emptyList<SykepengesoeknadMelding>())
                } else {
                    try {
                        emit(pollMessages())
                    } catch (e: WakeupException) {
                        logger.info("SykepengesoeknadConsumer mottok wakeup-signal og avslutter")
                        break
                    }
                }
            }
        }.onEach { it ->
            logger.debug { "flex messages received :" + it.size + "on topic " + config.flexTopic }
            it.forEach {  record ->service.behandle(record) }
        }.onEach {
            try {
                consumer.commitSync()
            } catch (e: CommitFailedException) {
                logger.error { "Commit feilet med feilmeldingen: ${e.message}" }
            } catch (e: WakeupException) {
                logger.info("SykepengesoeknadConsumer mottok wakeup-signal under commit og avslutter")
            }
        }.onEach {
            Metrics.incProcessedVurderingerTotal(it.count())
        }

    fun stop() {
        running.set(false)
        consumer.wakeup()
    }

    fun close() {
        consumer.close()
    }
}