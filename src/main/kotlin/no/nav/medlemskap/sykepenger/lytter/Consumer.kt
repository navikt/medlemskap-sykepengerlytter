package no.nav.medlemskap.sykepenger.lytter

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.time.delay
import no.nav.medlemskap.sykepenger.lytter.config.KafkaConfig
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import no.nav.medlemskap.sykepenger.lytter.service.LovMeService
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class Consumer(
    environment: Environment,
    private val config: KafkaConfig = KafkaConfig(environment),
    private val service: LovMeService = LovMeService(environment = environment),
    private val consumer: KafkaConsumer<String, String> = config.createConsumer(),
) {

    init {
        consumer.subscribe(listOf(config.topic))
    }

    fun pollMessages(): List<SoknadRecord> = //listOf("Message A","Message B","Message C")

        consumer.poll(Duration.ofSeconds(4))
            .map { SoknadRecord(it.partition(),
                it.offset(),
                it.value(),
                it.key(),
                it.topic(),
                JaksonParser().parse(it.value())
            )}
            .also {
                //Metrics.incReceivedTotal(it.count())
                //it.forEach { hendelse ->
                //    Metrics.incReceivedKilde(hendelse.kilde)
                //}
            }


            //.filter { it.kilde == Hendelse.Kilde.KDI }

    fun flow(): Flow<List<SoknadRecord>> =
        flow {
            while (true) {
                emit(pollMessages())
                delay(Duration.ofSeconds(5))
            }
        }.onEach {
            println("receiced :"+ it.size)
            it.forEach { record -> service.handle(record) }
        }.onEach {
            consumer.commitAsync()
        }.onEach {
            Metrics.incProcessedTotal(it.count())
        }

}