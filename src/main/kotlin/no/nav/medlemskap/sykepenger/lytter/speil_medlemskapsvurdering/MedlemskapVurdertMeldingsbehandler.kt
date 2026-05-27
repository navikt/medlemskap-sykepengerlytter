package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.MarkerFactory

class MedlemskapVurdertMeldingsbehandler(
    private val medlemskapVurdertMapper: MedlemskapVurdertMapper = MedlemskapVurdertMapper(),
    private val finnMedlemskapsvurdering: FinnMedlemskapsvurdering = FinnMedlemskapsvurdering(),
) {

    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun behandle(record: ConsumerRecord<String, String>): SpeilRespons? {
        val vurderingFraMelding = medlemskapVurdertMapper.tilMedlemskapVurdering(record.value())
        log.info(
            teamLogs, "Mottatt Kafka melding fra medlemskap vurdert for person: ${vurderingFraMelding.fnr}",
            kv("callId", record.key()),
            kv("ytelse", vurderingFraMelding.ytelse),
        )
        val speilRespons = finnMedlemskapsvurdering.finn(vurderingFraMelding)
            ?: run {
                log.info(
                    teamLogs,
                    "Medlemskapsvurderingen gjelder for kilde: ${vurderingFraMelding.kanal} og ytelsen: ${vurderingFraMelding.ytelse}, og behandles ikke videre.",
                    kv("callId", record.key()),
                    kv("fnr", vurderingFraMelding.fnr)
                )
                return null
            }

        log.info(
            teamLogs,
            "SpeilRespons opprettet fra medlemskapsvurdering for person: ${vurderingFraMelding.fnr}",
            kv("callId", record.key()),
            kv("svar", vurderingFraMelding.svar),
            kv("status", vurderingFraMelding.status),
            kv("speilSvar", speilRespons.speilSvar),
            kv("inneholder nye brukerspørsmål", vurderingFraMelding.brukerinput.utfortAarbeidUtenforNorge != null)
        )
        return speilRespons
    }
}
