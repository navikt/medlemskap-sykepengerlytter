package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.service.TidligereBrukersvar
import org.slf4j.MarkerFactory

class UtledBrukerspørsmål(
    private val tidligereBrukersvar: TidligereBrukersvar
) {
    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun finnTidligereStilteSpørsmål(medlemskapOppslagRequest: MedlOppslagRequest): List<Spørsmål> =
        tidligereBrukersvar
            .finnNyesteMedSvarInnenforLevetid(
                medlemskapOppslagRequest.fnr,
                medlemskapOppslagRequest.førsteDagForYtelse
            )
            ?.tilTidligereStilteSpørsmål()
            .also { spørsmål ->
                if (spørsmål != null) {
                    log.info(
                        teamLogs,
                        "Fant følgende tidligere brukersvar innenfor levetiden med gyldig svartype for ${medlemskapOppslagRequest.fnr}: ${
                            spørsmål.joinToString(", ")
                        }"
                    )
                }
            }
            ?: emptyList()

    private fun Brukersporsmaal.tilTidligereStilteSpørsmål() = listOfNotNull(
        utfort_arbeid_utenfor_norge.taHvis { svar.erNei() }?.let { Spørsmål.ARBEID_UTENFOR_NORGE },
        oppholdUtenforNorge.taHvis { svar.erNei() }?.let { Spørsmål.OPPHOLD_UTENFOR_NORGE },
        oppholdUtenforEOS.taHvis { svar.erNei() }?.let { Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE },
        oppholdstilatelse.taHvis { svar.erJa() }?.let { Spørsmål.OPPHOLDSTILATELSE }
    )

    private inline fun <T> T?.taHvis(predicate: T.() -> Boolean): T? =
        this?.takeIf(predicate)

    private fun Boolean.erJa() = this

    private fun Boolean.erNei() = !this
}
