package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.service.TidligereBrukersvar
import org.slf4j.MarkerFactory

class HentGjenbrukbareBrukerspoersmaal(
    private val tidligereBrukersvar: TidligereBrukersvar
) {
    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun finnGjenbrukbareSpørsmål(medlemskapOppslagRequest: MedlOppslagRequest): List<Spørsmål> =
        tidligereBrukersvar
            .finnNyesteGjenbrukbareSvar(
                medlemskapOppslagRequest.fnr,
                medlemskapOppslagRequest.førsteDagForYtelse
            )
            ?.tilGjenbrukbareSpørsmål()
            .also { spørsmål ->
                if (spørsmål != null) {
                    log.info(
                        teamLogs,
                        "Fant følgende tidligere gjenbrukbare brukersvar innenfor levetiden med gyldig svartype for ${medlemskapOppslagRequest.fnr}: ${
                            spørsmål.joinToString(", ")
                        }"
                    )
                }
            }
            ?: emptyList()

    private fun Brukerspørsmål.tilGjenbrukbareSpørsmål() = listOfNotNull(
        utfort_arbeid_utenfor_norge?.let { Spørsmål.ARBEID_UTENFOR_NORGE },
        oppholdUtenforNorge?.let { Spørsmål.OPPHOLD_UTENFOR_NORGE },
        oppholdUtenforEOS?.let { Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE },
        oppholdstilatelse?.let { Spørsmål.OPPHOLDSTILATELSE }
    )
}
