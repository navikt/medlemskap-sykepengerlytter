package no.nav.medlemskap.sykepenger.lytter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.config.KafkaConfig
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecordReplay
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.service.SoknadRecordHandler
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class MedlemskapVurdertConsumer(
    environment: Environment,
    private val config: KafkaConfig = KafkaConfig(environment),
    private val persistenceService: PersistenceService = PersistenceService(
        PostgresMedlemskapVurdertRepository(DataSourceBuilder(environment).getDataSource())
    ),
    private val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService),

    private val consumer: KafkaConsumer<String, String> = config.createReplayConsumer(),

    ) {
    private val secureLogger = KotlinLogging.logger("tjenestekall")
    private val logger = KotlinLogging.logger { }

    init {
        consumer.subscribe(listOf(config.replayTopic))
    }

    fun pollMessages(): List<SoknadRecordReplay> =

        consumer.poll(Duration.ofSeconds(4))
            .map {
                SoknadRecordReplay(
                    it.partition(),
                    it.offset(),
                    it.value(),
                    it.key(),
                    it.topic(),
                    JacksonParser().parse(it.value()) ,
                    LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(it!!.timestamp()), ZoneId.systemDefault()),
                    it.timestampType().name
                )
            }
            .also {
                Metrics.incReceivedvurderingTotal(it.count())
            }

    fun flow(): Flow<List<SoknadRecordReplay>> =
        flow {
            while (true) {

                if (config.medlemskapvurdert_enabled != "Ja") {
                    logger.debug("Kafka is disabled. Does not fetch messages from topic")
                    emit(emptyList<SoknadRecordReplay>())
                } else {
                    emit(pollMessages())
                }
            }
        }.onEach {
            logger.debug { "Replay received :" + it.size + "on topic " + config.topic }
            it.forEach { record -> service.replay(SoknadRecord(record.partition,record.offset,record.value,record.key,record.topic,record.sykepengeSoknad),record.timestamp) }
        }.onEach {
            consumer.commitAsync()
        }.onEach {
            Metrics.incProcessedVurderingerTotal(it.count())
        }

}