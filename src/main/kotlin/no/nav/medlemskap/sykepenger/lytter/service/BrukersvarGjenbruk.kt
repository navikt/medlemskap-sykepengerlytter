package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.UtfortAarbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import org.slf4j.MarkerFactory

class BrukersvarGjenbruk(val finnForrigeBrukersvar: FinnForrigeBrukersvar) {

    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    private val mapBrukersvar: MapBrukersvar = MapBrukersvar

    fun vurderGjenbrukAvBrukersvar(
        søknadsParametere: SoeknadsParametere, brukersvarPåSøknad: Brukersporsmaal? = null
    ): Brukerinput {
        val utførtArbeidUtenforNorge =
            mapBrukersvar.mapUtførtArbeidUtenforNorge(brukersvarPåSøknad?.utfort_arbeid_utenfor_norge)

        return when {
            søknadInneholderNyeBrukerspørsmål(utførtArbeidUtenforNorge) -> {
                log.info(teamLogs, "Søknad med callId: ${søknadsParametere.callId} inneholder nye brukerspørsmål")
                mapTilBrukerinput(brukersvarPåSøknad, utførtArbeidUtenforNorge)
            }

            søknadInneholderGammeltBrukerspørsmålMedSvarJa(brukersvarPåSøknad) -> {
                log.info(
                    teamLogs,
                    "Søknad med callId: ${søknadsParametere.callId} inneholder gammelt brukerspørsmål med svar JA"
                )
                mapTilBrukerinput(arbeidUtenforNorge = true)
            }

            // Søknad inneholder gammelt brukerspørsmål med svar NEI eller kommer uten brukersvar
            else -> {
                val forrigeBrukersvar = finnForrigeBrukersvar.finnForrigeBrukersvar(
                    søknadsParametere.fnr,
                    søknadsParametere.førsteDagForYtelse
                )

                if (forrigeBrukersvar == null) {
                    log.info(
                        teamLogs,
                        "Ingen gjenbrukbare brukersvar funnet for person: ${søknadsParametere.fnr}. Setter gammelt brukerspørsmål til Nei"
                    )
                    return mapTilBrukerinput(arbeidUtenforNorge = false)
                }

                log.info(
                    teamLogs,
                    "Gjenbruker brukersvar funnet for person: ${søknadsParametere.fnr} fra tidligere søknad med eventDate: ${forrigeBrukersvar.eventDate}"
                )

                val utførtArbeidUtenforNorge =
                    mapBrukersvar.mapUtførtArbeidUtenforNorge(forrigeBrukersvar.utfort_arbeid_utenfor_norge)

                return mapTilBrukerinput(forrigeBrukersvar, utførtArbeidUtenforNorge)
            }
        }
    }

    private fun søknadInneholderNyeBrukerspørsmål(utførtArbeidUtenforNorge: UtfortAarbeidUtenforNorge?): Boolean =
        utførtArbeidUtenforNorge != null

    private fun søknadInneholderGammeltBrukerspørsmålMedSvarJa(brukersvarPåSøknad: Brukersporsmaal?): Boolean =
        brukersvarPåSøknad?.sporsmaal?.arbeidUtland == true


    private fun mapTilBrukerinput(arbeidUtenforNorge: Boolean): Brukerinput =
        Brukerinput(arbeidUtenforNorge = arbeidUtenforNorge)


    private fun mapTilBrukerinput(
        brukersvar: Brukersporsmaal?,
        utførtArbeidUtenforNorge: UtfortAarbeidUtenforNorge?
    ): Brukerinput {
        return Brukerinput(
            arbeidUtenforNorge = mapBrukersvar.kopierFraUtførtArbeidUtenforNorge(
                utførtArbeidUtenforNorge?.svar ?: false
            ),
            oppholdstilatelse = mapBrukersvar.mapOppholdstillatelse(brukersvar?.oppholdstilatelse),
            utfortAarbeidUtenforNorge = utførtArbeidUtenforNorge,
            oppholdUtenforEos = mapBrukersvar.mapOppholdUtenforEos(brukersvar?.oppholdUtenforEOS),
            oppholdUtenforNorge = mapBrukersvar.mapOppholdUtenforNorge(brukersvar?.oppholdUtenforNorge)
        )
    }
}
