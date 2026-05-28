package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.domain.inneholderNyModellForBrukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka.SpeilResponsPublisher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MarkerFactory

class SpeilResponsBehandler(
    private val medlemskapsvurderingMapper: MedlemskapsvurderingMapper = MedlemskapsvurderingMapper(),
    private val speilResponsMapper: SpeilResponsMapper = SpeilResponsMapper(),
    private val speilResponsPublisher: SpeilResponsPublisher,
) {

    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun behandle(record: ConsumerRecord<String, String>) {
        val medlemskapsvurdering = medlemskapsvurderingMapper.tilMedlemskapsvurdering(record.value())
        log.info(
            teamLogs, "Mottatt Kafka melding fra medlemskap vurdert for person: ${medlemskapsvurdering.fnr}",
            kv("callId", record.key()),
            kv("ytelse", medlemskapsvurdering.ytelse),
        )
        val speilRespons = speilResponsMapper.tilSpeilRespons(medlemskapsvurdering)
            ?: run {
                log.info(
                    teamLogs, "Medlemskapsvurderingen for person: ${medlemskapsvurdering.fnr} gjelder for kilde: ${medlemskapsvurdering.kanal}" +
                            " og ytelsen: ${medlemskapsvurdering.ytelse} er ikke støttet. Støtter kilde 'kafka' og ytelsen 'SYKEPENGER'.",
                    kv("callId", record.key())
                )
                return
            }

        log.info(
            teamLogs, "SpeilRespons opprettet for person: ${medlemskapsvurdering.fnr}",
            kv("callId", record.key()),
            kv("speilrespons", speilRespons),
            kv(
                "inneholder ny modell for brukerspørsmål",
                medlemskapsvurdering.brukerinput.inneholderNyModellForBrukerspørsmål()
            )
        )
        speilResponsPublisher.publiser(speilRespons)
    }
}
