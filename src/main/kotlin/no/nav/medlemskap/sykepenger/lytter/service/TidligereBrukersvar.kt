package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import org.slf4j.MarkerFactory
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

class TidligereBrukersvar(private val persistenceService: PersistenceService) {

    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun finnNyesteGjenbrukbareSvar(
        fnr: String,
        førsteDagForYtelse: String
    ): Brukersporsmaal? =
        persistenceService
            .hentbrukersporsmaalForFnr(fnr)
            .filter { spm ->
                antallDagerMellomToDatoer(spm.eventDate, LocalDate.parse(førsteDagForYtelse)) < Levetid.STANDARD_LEVETID_32.dager
            }
            .filter { spm -> spm.normaliser().erGjenbrukbart() }
            .maxByOrNull { it.eventDate }
            .also { loggNyesteBrukersvar(fnr, it) }

    private fun loggNyesteBrukersvar(fnr: String, brukersvar: Brukersporsmaal?) {
        if (brukersvar == null) {
            log.info(
                teamLogs,
                "Fant ingen tidligere brukersvar innenfor levetid på ${Levetid.STANDARD_LEVETID_32.dager} dager for $fnr"
            )
        } else {
            log.info(
                teamLogs,
                "Nyeste brukersvar funnet for $fnr med id=${brukersvar.soknadid}, eventDate=${brukersvar.eventDate}"
            )
        }
    }

    private fun antallDagerMellomToDatoer(førsteDato: LocalDate, andreDato: LocalDate): Int =
        ChronoUnit.DAYS.between(førsteDato, andreDato).toInt().absoluteValue
}
