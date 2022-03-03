package no.nav.medlemskap.sykepenger.lytter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.config.KafkaConfig
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.sykepenger.lytter.jakson.MedlemskapVurdertParser
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class MedlemskapVurdertConsumer(
    environment: Environment,
    private val config: KafkaConfig = KafkaConfig(environment),
    private val persistenceService: PersistenceService = PersistenceService(
        PostgresMedlemskapVurdertRepository(DataSourceBuilder(environment).getDataSource())
    ),

    private val consumer: KafkaConsumer<String, String> = config.createMedlemskapVurdertConsumer(),

    )
{
    private val secureLogger = KotlinLogging.logger("tjenestekall")
    private val logger = KotlinLogging.logger { }
    init {
        consumer.subscribe(listOf(config.medlemskapVurdertTopic))
    }

    fun pollMessages(): List<MedlemskapVurdertRecord> =

        consumer.poll(Duration.ofSeconds(4))
            .map { MedlemskapVurdertRecord(it.partition(),
                it.offset(),
                it.value(),
                it.key(),
                it.topic(),
                MedlemskapVurdertParser().parse(it.value())
            )}
            .also {
                Metrics.incReceivedTotal(it.count())
            }

    fun flow(): Flow<List<MedlemskapVurdertRecord>> =
        flow {
            while (true) {

                if(config.medlemskapvurdert_enabled!="Ja"){
                    logger.debug("Kafka is disabled. Does not fetch messages from topic")
                    emit(emptyList<MedlemskapVurdertRecord>())
                }
                else{
                    emit(pollMessages())
                }

                delay(Duration.ofSeconds(1))
            }
        }.onEach {
            logger.debug { "received :"+ it.size + "on topic "+config.topic }
            it.forEach { record->persistenceService.handle(record) }
        }.onEach {
            consumer.commitAsync()
        }.onEach {
            Metrics.incProcessedTotal(it.count())
        }

}