package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.domain.inneholderNyModellForBrukerspørsmål
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MarkerFactory

class MedlemskapVurdertMeldingsbehandler(
    private val medlemskapsvurderingMapper: MedlemskapsvurderingMapper = MedlemskapsvurderingMapper(),
    private val speilResponsMapper: SpeilResponsMapper = SpeilResponsMapper(),
) {

    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun behandle(record: ConsumerRecord<String, String>): SpeilRespons? {
        val medlemskapsvurdering = medlemskapsvurderingMapper.tilMedlemskapsvurdering(record.value())
        log.info(
            teamLogs, "Mottatt Kafka melding fra medlemskap vurdert for person: ${medlemskapsvurdering.fnr}",
            kv("callId", record.key()),
            kv("ytelse", medlemskapsvurdering.ytelse),
        )
        val speilRespons = speilResponsMapper.mapTilSpeilRespons(medlemskapsvurdering)
            ?: run {
                log.info(
                    teamLogs,
                    "Medlemskapsvurderingen gjelder for kilde: ${medlemskapsvurdering.kanal} og ytelsen: ${medlemskapsvurdering.ytelse}, og behandles ikke videre.",
                    kv("callId", record.key()),
                    kv("fnr", medlemskapsvurdering.fnr)
                )
                return null
            }

        log.info(
            teamLogs,
            "SpeilRespons opprettet for medlemskapsvurdering for person: ${medlemskapsvurdering.fnr}",
            kv("callId", record.key()),
            kv("svar", medlemskapsvurdering.svar),
            kv("status", medlemskapsvurdering.status),
            kv("speilSvar", speilRespons.speilSvar),
            kv("inneholder ny modell for brukerspørsmål", medlemskapsvurdering.brukerinput.inneholderNyModellForBrukerspørsmål())
        )
        return speilRespons
    }
}
