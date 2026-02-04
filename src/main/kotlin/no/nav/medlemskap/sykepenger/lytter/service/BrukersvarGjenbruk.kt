package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.UtfortAarbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import org.slf4j.MarkerFactory

class BrukersvarGjenbruk {

    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    private val mapBrukersvar: MapBrukersvar = MapBrukersvar

    fun vurderGjenbrukAvBrukersvar(
        søknadsParametere: SoeknadsParametere,
        persistenceService: PersistenceService
    ): Brukerinput {
        val brukersvarPåSøknad = persistenceService.hentbrukersporsmaalForSoknadID(søknadsParametere.callId)
            ?: return mapTilBrukerinput(arbeidUtenforNorge = false)

        val utførtArbeidUtenforNorge = mapBrukersvar.mapUtførtArbeidUtenforNorge(brukersvarPåSøknad.utfort_arbeid_utenfor_norge)

        return when {
            søknadInneholderNyeBrukerspørsmål(utførtArbeidUtenforNorge) -> {
                log.info(teamLogs, "Søknad med callId: ${søknadsParametere.callId} inneholder nye brukerspørsmål")
                mapTilBrukerinput(brukersvarPåSøknad, utførtArbeidUtenforNorge)
            }
            søknadInneholderGammeltBrukerspørsmålMedSvarJa(brukersvarPåSøknad) -> {
                log.info(teamLogs, "Søknad med callId: ${søknadsParametere.callId} inneholder gammelt brukerspørsmål med svar JA")
                mapTilBrukerinput(arbeidUtenforNorge = true)
            }

            // Søknad inneholder gammelt brukerspørsmål med svar NEI
            else -> {
                val forrigeBrukersvar = finnForrigeBrukersvar(
                        søknadsParametere.fnr,
                        søknadsParametere.førsteDagForYtelse,
                        persistenceService
                    )
                        ?: return mapTilBrukerinput(arbeidUtenforNorge = false)

                log.info(teamLogs, "Fant gjenbrukbare brukersvar fra tidligere søknad med dato: ${forrigeBrukersvar.eventDate}")
                val utførtArbeidUtenforNorge = mapBrukersvar.mapUtførtArbeidUtenforNorge(forrigeBrukersvar.utfort_arbeid_utenfor_norge)
                mapTilBrukerinput(forrigeBrukersvar, utførtArbeidUtenforNorge)
            }
        }
    }

    private fun søknadInneholderNyeBrukerspørsmål(utførtArbeidUtenforNorge: UtfortAarbeidUtenforNorge? ): Boolean =
        utførtArbeidUtenforNorge != null

    private fun søknadInneholderGammeltBrukerspørsmålMedSvarJa(brukersvarPåSøknad: Brukersporsmaal): Boolean =
        brukersvarPåSøknad.sporsmaal?.arbeidUtland == true


    private fun mapTilBrukerinput(arbeidUtenforNorge: Boolean): Brukerinput =
        Brukerinput(arbeidUtenforNorge = arbeidUtenforNorge)


    private fun mapTilBrukerinput(brukersvar: Brukersporsmaal, utførtArbeidUtenforNorge: UtfortAarbeidUtenforNorge?): Brukerinput {
        return Brukerinput(
            arbeidUtenforNorge = mapBrukersvar.kopierFraUtførtArbeidUtenforNorge(
                utførtArbeidUtenforNorge?.svar ?: false
            ),
            oppholdstilatelse = mapBrukersvar.mapOppholdstillatelse(brukersvar.oppholdstilatelse),
            utfortAarbeidUtenforNorge = utførtArbeidUtenforNorge,
            oppholdUtenforEos = mapBrukersvar.mapOppholdUtenforEos(brukersvar.oppholdUtenforEOS),
            oppholdUtenforNorge = mapBrukersvar.mapOppholdUtenforNorge(brukersvar.oppholdUtenforNorge)
        )
    }
}
