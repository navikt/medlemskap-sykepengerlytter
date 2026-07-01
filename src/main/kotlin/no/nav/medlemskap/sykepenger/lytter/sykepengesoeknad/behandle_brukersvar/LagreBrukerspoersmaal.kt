package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import org.slf4j.MarkerFactory

class LagreBrukerspoersmaal(
    private val persistenceService: PersistenceService,
    private val brukersvarDuplikatsjekk: BrukersvarDuplikatsjekk = BrukersvarDuplikatsjekk(persistenceService)
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    fun lagre(brukerspørsmål: Brukersporsmaal) {
        if (brukersvarDuplikatsjekk.erLagretFraFør(brukerspørsmål)) {
            loggFiltrertDuplikat(brukerspørsmål)
            return
        }

        lagreBrukerspørsmål(brukerspørsmål)
    }

    private fun lagreBrukerspørsmål(brukerspørsmål: Brukersporsmaal) {
        persistenceService.lagreBrukersporsmaal(brukerspørsmål)
        log.info(
            teamLogs,
            "Brukerspørsmål for søknad ${brukerspørsmål.soknadid} lagret til databasen",
            kv("callId", brukerspørsmål.soknadid),
            kv("dato", brukerspørsmål.eventDate)
        )
    }

    private fun loggFiltrertDuplikat(brukerspørsmål: Brukersporsmaal) {
        log.info(
            teamLogs,
            "Flex melding for søknad ${brukerspørsmål.soknadid}, filtrert ut. duplikat melding: ${brukerspørsmål.soknadid}"
        )
    }
}
