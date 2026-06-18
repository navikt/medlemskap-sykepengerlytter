package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.kafka

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.nais.Metrics
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadMottak
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadRecord
import org.apache.kafka.clients.consumer.CommitFailedException
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class BrukerSporsmaalConsumer(
    environment: Environment,
    private val persistenceService: PersistenceService = PersistenceService(
        PostgresMedlemskapVurdertRepository(DataSourceBuilder(environment).getDataSource()),
        PostgresBrukersporsmaalRepository(DataSourceBuilder(environment).getDataSource())
    ),
    private val config: SykepengeSoeknadKafkaConfig = SykepengeSoeknadKafkaConfig(environment),
    private val service: SykepengesoeknadMottak = SykepengesoeknadMottak(persistenceService),
    private val consumer: KafkaConsumer<String, String> = config.createFlexConsumer(),

    ) {

    private val logger = KotlinLogging.logger { }

    init {
        consumer.subscribe(listOf(config.flexTopic))
    }

    fun pollMessages(): List<SykepengesoeknadRecord> =

        consumer.poll(Duration.ofSeconds(4))
            .map {
                SykepengesoeknadRecord(
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

    fun flow(): Flow<List<SykepengesoeknadRecord>> =
        kotlinx.coroutines.flow.flow {
            while (true) {

                if (config.brukersporsmaal_enabled != "Ja") {
                    logger.debug("Kafka is disabled. Does not fetch messages from topic")
                    emit(emptyList<SykepengesoeknadRecord>())
                } else {
                    emit(pollMessages())
                }
            }
        }.onEach { it ->
            logger.debug { "flex messages received :" + it.size + "on topic " + config.flexTopic }
            it.forEach {  record ->service.handle(record) }
        }.onEach {
            try {
                consumer.commitSync()
            } catch (e: CommitFailedException) {
                logger.error { "Commit feilet med feilmeldingen: ${e.message}" }
            }
        }.onEach {
            Metrics.incProcessedVurderingerTotal(it.count())
        }

}