package no.nav.medlemskap.sykepenger.lytter.service


import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.nyesteMedSvar
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue


fun finnForrigeBrukerspørsmål(
    lovmeRequest: MedlOppslagRequest,
    persistenceService: PersistenceService
): List<Spørsmål> {

    val førsteDagForYtelse = LocalDate.parse(lovmeRequest.førsteDagForYtelse)

    return persistenceService
        .hentbrukersporsmaalForFnr(lovmeRequest.fnr)
        .filter { spm ->
            antallDagerMellomToDatoer(spm.eventDate, førsteDagForYtelse) < Levetid.STANDARD_LEVETID_32.dager
        }
        .nyesteMedSvar()
        ?.let(::finnForrigeBrukerspørsmålFra)
        ?: emptyList()
}


private inline fun <T> taHvis(felt: T?, predicate: T.() -> Boolean) =
    felt?.takeIf(predicate)

fun finnForrigeBrukerspørsmålFra(forrigeBrukersvar: Brukersporsmaal?) = listOfNotNull(
    taHvis(forrigeBrukersvar?.utfort_arbeid_utenfor_norge) { neiSvar(svar) }?.let { Spørsmål.ARBEID_UTENFOR_NORGE },
    taHvis(forrigeBrukersvar?.oppholdUtenforNorge) { neiSvar(svar) }?.let { Spørsmål.OPPHOLD_UTENFOR_NORGE },
    taHvis(forrigeBrukersvar?.oppholdUtenforEOS) { neiSvar(svar) }?.let { Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE },
    taHvis(forrigeBrukersvar?.oppholdstilatelse) { jaSvar(svar) }?.let { Spørsmål.OPPHOLDSTILATELSE }
)

fun jaSvar(svar: Boolean) = svar

fun neiSvar(svar: Boolean) = !svar

fun antallDagerMellomToDatoer(førsteDato: LocalDate, andreDato: LocalDate): Int =
    ChronoUnit.DAYS.between(førsteDato, andreDato).toInt().absoluteValue