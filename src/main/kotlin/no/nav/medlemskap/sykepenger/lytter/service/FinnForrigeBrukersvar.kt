package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.nyesteMedSvar
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import org.slf4j.MarkerFactory
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

class FinnForrigeBrukersvar(val persistenceService: PersistenceService) {

    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun finnForrigeBrukersvar(
        fnr: String,
        førsteDagForYtelse: String
    ): Brukersporsmaal? {
        return persistenceService
            .hentbrukersporsmaalForFnr(fnr)
            .filter { spm ->
                antallDagerMellomToDatoer(spm.eventDate, LocalDate.parse(førsteDagForYtelse)) < Levetid.STANDARD_LEVETID_32.dager
            }
            .nyesteMedSvar()
            .also { kanskjeNyeste ->
                if (kanskjeNyeste == null) {
                    log.info(
                        teamLogs,
                        "Fant ingen tidligere brukersvar innenfor levetid på ${Levetid.STANDARD_LEVETID_32.dager} dager for $fnr"
                    )
                } else {
                    log.info(
                        teamLogs,
                        "Nyeste brukersvar funnet: id=${kanskjeNyeste.soknadid}, eventDate=${kanskjeNyeste.eventDate}"
                    )
                }
            }
    }


    fun finnForrigeStilteBrukerspørsmål(
        fnr: String,
        førsteDagForYtelse: String
    ): List<Spørsmål> {
        return persistenceService
            .hentbrukersporsmaalForFnr(fnr)
            .filter { spm ->
                antallDagerMellomToDatoer(spm.eventDate, LocalDate.parse(førsteDagForYtelse)) < Levetid.STANDARD_LEVETID_32.dager
            }
            .nyesteMedSvar()
            .also { kanskjeNyeste ->
                if (kanskjeNyeste == null) {
                    log.info(
                        teamLogs,
                        "Fant ingen tidligere brukersvar innenfor levetid på ${Levetid.STANDARD_LEVETID_32.dager} dager for $fnr"
                    )
                } else {
                    log.info(
                        teamLogs,
                        "Nyeste brukersvar funnet: id=${kanskjeNyeste.soknadid}, eventDate=${kanskjeNyeste.eventDate}"
                    )
                }
            }
            ?.let(::finnForrigeBrukerspørsmålFra)
            .also { kanskjeNyeste ->
                if (kanskjeNyeste != null) {
                    log.info(
                        teamLogs,
                        "Fant følgende tidligere brukersvar innenfor levetiden med gyldig svartype for $fnr: ${
                            kanskjeNyeste.joinToString(", ")
                        }"
                    )
                }
            }
            ?: emptyList()
    }


    private inline fun <T> taHvis(felt: T?, predicate: T.() -> Boolean) =
        felt?.takeIf(predicate)

    private fun finnForrigeBrukerspørsmålFra(forrigeBrukersvar: Brukersporsmaal?) = listOfNotNull(
        taHvis(forrigeBrukersvar?.utfort_arbeid_utenfor_norge) { neiSvar(svar) }?.let { Spørsmål.ARBEID_UTENFOR_NORGE },
        taHvis(forrigeBrukersvar?.oppholdUtenforNorge) { neiSvar(svar) }?.let { Spørsmål.OPPHOLD_UTENFOR_NORGE },
        taHvis(forrigeBrukersvar?.oppholdUtenforEOS) { neiSvar(svar) }?.let { Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE },
        taHvis(forrigeBrukersvar?.oppholdstilatelse) { jaSvar(svar) }?.let { Spørsmål.OPPHOLDSTILATELSE }
    )

    private fun jaSvar(svar: Boolean) = svar

    private fun neiSvar(svar: Boolean) = !svar

    private fun antallDagerMellomToDatoer(førsteDato: LocalDate, andreDato: LocalDate): Int =
        ChronoUnit.DAYS.between(førsteDato, andreDato).toInt().absoluteValue


}