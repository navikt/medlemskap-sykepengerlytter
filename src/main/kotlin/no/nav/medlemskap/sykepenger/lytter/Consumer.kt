package no.nav.medlemskap.sykepenger.lytter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.config.KafkaConfig
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.service.SoknadRecordHandler
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class Consumer(
    environment: Environment,
    private val persistenceService: PersistenceService = PersistenceService(
        PostgresMedlemskapVurdertRepository(DataSourceBuilder(environment).getDataSource()) ,
        PostgresBrukersporsmaalRepository(DataSourceBuilder(environment).getDataSource())
    ),
    private val config: KafkaConfig = KafkaConfig(environment),
    private val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService),
    private val consumer: KafkaConsumer<String, String> = config.createConsumer(),

    ) {
    private val secureLogger = KotlinLogging.logger("tjenestekall")
    private val logger = KotlinLogging.logger { }

    init {
        consumer.subscribe(listOf(config.topic))
    }

    fun pollMessages(): List<SoknadRecord> =

        consumer.poll(Duration.ofSeconds(4))
            .map {
                SoknadRecord(
                    it.partition(),
                    it.offset(),
                    it.value(),
                    it.key(),
                    it.topic(),
                    JacksonParser().parse(it.value())
                )
            }
            .also {
                Metrics.incReceivedTotal(it.count())
            }

    fun flow(): Flow<List<SoknadRecord>> =
        flow {
            while (true) {

                if (config.enabled != "Ja") {
                    logger.debug("Kafka is disabled. Does not fetch messages from topic")
                    emit(emptyList<SoknadRecord>())
                } else {
                    emit(pollMessages())
                }

                delay(Duration.ofMinutes(1))
            }
        }.onEach {
            logger.debug { "receiced :" + it.size + "on topic " + config.topic }
            it.forEach { record -> service.handle(record) }
        }.onEach {
            consumer.commitAsync()
        }.onEach {
            Metrics.incProcessedTotal(it.count())
        }

}