package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadRecord
import org.slf4j.MarkerFactory

class BehandleBrukersvar(
    private val persistenceService: PersistenceService,
    private val brukersvarMapper: BrukersvarMapper = BrukersvarMapper(),
    private val brukersvarDuplikatsjekk: BrukersvarDuplikatsjekk = BrukersvarDuplikatsjekk(persistenceService)
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    /*
     * SP1220
     * */
    fun behandle(sykepengesøknadRecord: SykepengesoeknadRecord) {
        val brukersporsmål: Brukersporsmaal = brukersvarMapper.mapMessage(sykepengesøknadRecord)

        if (brukersvarDuplikatsjekk.erLagretFraFør(brukersporsmål)) {
            loggFiltrertDuplikat(sykepengesøknadRecord, brukersporsmål)
            return
        }

        lagreBrukersporsmaal(sykepengesøknadRecord, brukersporsmål)
    }

    private fun lagreBrukersporsmaal(
        sykepengesoeknadRecord: SykepengesoeknadRecord,
        brukersporsmaal: Brukersporsmaal
    ) {
        persistenceService.lagreBrukersporsmaal(brukersporsmaal)
        log.info(
            teamLogs,
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
            teamLogs,
            "Flex melding for søknad ${sykepengesoeknadRecord.key}, " +
                    "offset : ${sykepengesoeknadRecord.offset}, " +
                    "partition : ${sykepengesoeknadRecord.partition}," +
                    "filtrert ut. duplikat melding: ${brukersporsmaal.soknadid}"
        )
    }
}
