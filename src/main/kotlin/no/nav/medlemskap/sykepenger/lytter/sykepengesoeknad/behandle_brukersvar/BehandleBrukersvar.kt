package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadMelding
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
    fun behandle(sykepengesøknadRecord: SykepengesoeknadMelding) {
        val brukersporsmål: Brukersporsmaal = brukersvarMapper.mapMessage(sykepengesøknadRecord)

        if (brukersvarDuplikatsjekk.erLagretFraFør(brukersporsmål)) {
            loggFiltrertDuplikat(sykepengesøknadRecord, brukersporsmål)
            return
        }

        lagreBrukersporsmaal(sykepengesøknadRecord, brukersporsmål)
    }

    private fun lagreBrukersporsmaal(
        sykepengesøknadMelding: SykepengesoeknadMelding,
        brukerspørsmål: Brukersporsmaal
    ) {
        persistenceService.lagreBrukersporsmaal(brukerspørsmål)
        log.info(
            teamLogs,
            "Brukerspørsmål for søknad ${sykepengesøknadMelding.key} lagret til databasen",
            kv("callId", sykepengesøknadMelding.key),
            kv("dato", brukerspørsmål.eventDate),
            kv("partition", sykepengesøknadMelding.partition),
        )
    }

    private fun loggFiltrertDuplikat(
        sykepengesøknadMelding: SykepengesoeknadMelding,
        brukerspørsmål: Brukersporsmaal
    ) {
        log.info(
            teamLogs,
            "Flex melding for søknad ${sykepengesøknadMelding.key}, " +
                    "offset : ${sykepengesøknadMelding.offset}, " +
                    "partition : ${sykepengesøknadMelding.partition}," +
                    "filtrert ut. duplikat melding: ${brukerspørsmål.soknadid}"
        )
    }
}
