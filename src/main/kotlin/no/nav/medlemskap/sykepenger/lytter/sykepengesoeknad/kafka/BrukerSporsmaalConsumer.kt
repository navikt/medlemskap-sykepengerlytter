package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.kafka

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.nais.Metrics
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadMottak
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadMelding
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.BehandleSykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.LagreVurderingsstatus
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.SykepengesoeknadFiltrering
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.LagreBrukerspoersmaal
import org.apache.kafka.clients.consumer.CommitFailedException
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class BrukerSporsmaalConsumer(
    private val config: SykepengeSoeknadKafkaConfig,
    private val service: SykepengesoeknadMottak,
    private val consumer: KafkaConsumer<String, String>,
    ) {

    private val logger = KotlinLogging.logger { }

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
            while (true) {

                if (config.brukersporsmaal_enabled != "Ja") {
                    logger.debug("Kafka is disabled. Does not fetch messages from topic")
                    emit(emptyList<SykepengesoeknadMelding>())
                } else {
                    emit(pollMessages())
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
            }
        }.onEach {
            Metrics.incProcessedVurderingerTotal(it.count())
        }

    fun close() {
        consumer.close()
    }
}