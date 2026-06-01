package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.SpeilRespons
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.MarkerFactory

fun interface SpeilResponsPublisher {
    fun publiser(speilRespons: SpeilRespons)
}

class MedlemskapsvurderingerProducer(
    private val topic: String = MedlemskapsvurderingerProducerConfig.TOPIC,
    private val producer: Producer<String, String> = MedlemskapsvurderingerProducerConfig.createProducer(),
    private val objectMapper: ObjectMapper = no.nav.medlemskap.sykepenger.lytter.config.objectMapper,
) : SpeilResponsPublisher {

    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    override fun publiser(speilRespons: SpeilRespons) {
        val metadata = producer
            .send(
                ProducerRecord(
                    topic,
                    speilRespons.soknadId,
                    objectMapper.writeValueAsString(speilRespons),
                )
            )
            .get()

        log.info(
            teamLogs,
            "Publiserte SpeilRespons til $topic for person: ${speilRespons.fnr}",
            kv("partition", metadata.partition()),
            kv("offset", metadata.offset()),
            kv("callId", speilRespons.soknadId),
            kv("speilrespons", speilRespons)
        )
    }
}
