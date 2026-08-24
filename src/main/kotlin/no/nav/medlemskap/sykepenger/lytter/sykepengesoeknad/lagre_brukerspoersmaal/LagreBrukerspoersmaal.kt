package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål
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

    fun lagre(brukerspørsmål: Brukerspørsmål) {
        when (val resultat = brukerspørsmål.vurderLagring()) {
            is LagringResultat.Duplikat -> loggFiltrertDuplikat(resultat.brukerspørsmål)
            is LagringResultat.SkalLagres -> lagreBrukerspørsmål(resultat.brukerspørsmål)
        }
    }

    private fun Brukerspørsmål.vurderLagring(): LagringResultat {
        return if (brukersvarDuplikatsjekk.erLagretFraFør(this)) {
            LagringResultat.Duplikat(this)
        } else {
            LagringResultat.SkalLagres(this)
        }
    }

    private fun lagreBrukerspørsmål(brukerspørsmål: Brukerspørsmål) {
        persistenceService.lagreBrukersporsmaal(brukerspørsmål)
        log.info(
            teamLogs,
            "Brukerspørsmål for søknad ${brukerspørsmål.soknadid} lagret til databasen",
            kv("callId", brukerspørsmål.soknadid),
            kv("dato", brukerspørsmål.eventDate)
        )
    }

    private fun loggFiltrertDuplikat(brukerspørsmål: Brukerspørsmål) {
        log.info(
            teamLogs,
            "Brukerspørsmål for søknad ${brukerspørsmål.soknadid} for person ${brukerspørsmål.fnr} er duplikat og vil ikke bli lagret"
        )
    }

    private sealed interface LagringResultat {
        data class Duplikat(val brukerspørsmål: Brukerspørsmål) : LagringResultat
        data class SkalLagres(val brukerspørsmål: Brukerspørsmål) : LagringResultat
    }
}
