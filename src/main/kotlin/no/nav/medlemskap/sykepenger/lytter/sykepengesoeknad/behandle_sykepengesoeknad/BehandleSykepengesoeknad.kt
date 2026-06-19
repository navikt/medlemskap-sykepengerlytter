package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadRecord
import org.slf4j.MarkerFactory

open class BehandleSykepengesoeknad(
    private val sykepengesoeknadVurdering: SykepengesoeknadVurdering
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun behandle(sykepengesøknadRecord: SykepengesoeknadRecord) {
        val sykepengesøknad = JacksonParser().parse(sykepengesøknadRecord.value)

        if (!RegelmotorInngangskriterier.erOppfylt(sykepengesøknad)) {
            log.info(teamLogs, "melding filtrert ut da det ikke fyller kriterier for å bli sendt til regel motor",
                kv("x_data",JacksonParser().ToJson(sykepengesøknad).toPrettyString()),
                kv("callId", sykepengesøknadRecord.key)
            )
            return
        }

        val soknadRecord = SoknadRecord(sykepengesøknadRecord.partition, sykepengesøknadRecord.offset, sykepengesøknadRecord.value, sykepengesøknadRecord.key, sykepengesøknadRecord.topic, sykepengesøknad)
        sykepengesoeknadVurdering.vurder(soknadRecord)
    }
}
