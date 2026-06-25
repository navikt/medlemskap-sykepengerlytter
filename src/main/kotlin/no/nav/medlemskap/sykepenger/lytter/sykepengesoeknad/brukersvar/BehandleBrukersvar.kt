package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.brukersvar

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadRecord
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.Soknadstatus

class BehandleBrukersvar(
    private val persistenceService: PersistenceService,
    private val brukersvarMapper: BrukersvarMapper = BrukersvarMapper()
) {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    /*
     * SP1220
     * */
    fun behandleBrukerspørsmål(sykepengesoeknadRecord: SykepengesoeknadRecord) {
        val brukersporsmaal: Brukersporsmaal = brukersvarMapper.mapMessage(sykepengesoeknadRecord)

        if (brukerspørsmålErLagretFraFør(brukersporsmaal)) {
            loggFiltrertDuplikat(sykepengesoeknadRecord, brukersporsmaal)
            return
        }

        lagreBrukersporsmaal(sykepengesoeknadRecord, brukersporsmaal)
    }

    private fun brukerspørsmålErLagretFraFør(brukersporsmaal: Brukersporsmaal): Boolean =
        persistenceService.hentbrukersporsmaalForSoknadID(brukersporsmaal.soknadid) != null

    private fun lagreBrukersporsmaal(
        sykepengesoeknadRecord: SykepengesoeknadRecord,
        brukersporsmaal: Brukersporsmaal
    ) {
        persistenceService.lagreBrukersporsmaal(brukersporsmaal)
        log.info(
            "Brukerspørsmål for søknad ${sykepengesoeknadRecord.key} lagret til databasen",
            kv("callId", sykepengesoeknadRecord.key),
            kv("dato", brukersporsmaal.eventDate),
            kv("partition", sykepengesoeknadRecord.partition),
        )
    }

    private fun loggFiltrertDuplikat(
        sykepengesoeknadRecord: SykepengesoeknadRecord,
        brukersporsmaal: Brukersporsmaal
    ) {
        log.info(
            "Flex melding for søknad ${sykepengesoeknadRecord.key}, " +
                    "offset : ${sykepengesoeknadRecord.offset}, " +
                    "partition : ${sykepengesoeknadRecord.partition}," +
                    "filtrert ut. duplikat melding: ${brukersporsmaal.soknadid}"
        )
    }
}
