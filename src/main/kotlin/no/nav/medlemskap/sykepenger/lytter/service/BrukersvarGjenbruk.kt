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
        søknadsParametere: SoeknadsParametere,
        brukersvarPåInnkommendeSøknad: Brukersporsmaal? = null,
        kilde: String
    ): Brukerinput {
        val utførtArbeidUtenforNorge =
            mapBrukersvar.mapUtførtArbeidUtenforNorge(brukersvarPåInnkommendeSøknad?.utfort_arbeid_utenfor_norge)

        return when {
            søknadInneholderNyeBrukerspørsmål(utførtArbeidUtenforNorge) -> {
                log.info(
                    teamLogs,
                    "Søknad med callId: ${søknadsParametere.callId} for person: ${søknadsParametere.fnr} inneholder nye brukerspørsmål")
                mapTilBrukerinput(brukersvarPåInnkommendeSøknad, utførtArbeidUtenforNorge)
            }

            søknadInneholderGammeltBrukerspørsmålMedSvarJa(brukersvarPåInnkommendeSøknad) -> {
                log.info(
                    teamLogs,
                    "Søknad med callId: ${søknadsParametere.callId} for person ${søknadsParametere.fnr} inneholder gammelt brukerspørsmål med svar JA")
                mapTilBrukerinput(arbeidUtenforNorge = true)
            }

            // Søknad inneholder gammelt brukerspørsmål med svar NEI eller kommer uten brukersvar
            else -> {
                if (kilde == "speil") {
                    log.info(
                        teamLogs,
                        "Vurderer gjenbruk av tidligere brukersvar for forespørsel fra Speil for person: ${søknadsParametere.fnr}"
                    )
                } else {
                    log.info(
                        teamLogs,
                        "Vurderer gjenbruk av tidligere brukersvar for forespørsel fra Sykepengebackend for person: ${søknadsParametere.fnr}"
                    )
                }

                val forrigeBrukersvar = finnForrigeBrukersvar.finnForrigeBrukersvar(
                    søknadsParametere.fnr,
                    søknadsParametere.førsteDagForYtelse
                )

                if (forrigeBrukersvar == null) {
                    log.info(
                        teamLogs,
                        "Ingen gjenbrukbare brukersvar funnet for person: ${søknadsParametere.fnr}. Setter gammelt brukerspørsmål til standardverdi NEI"
                    )
                    return mapTilBrukerinput(arbeidUtenforNorge = false)
                }

                log.info(
                    teamLogs,
                    "Fant gjenbrukbart brukersvar. Gjenbruker brukersvaret funnet for person: ${søknadsParametere.fnr}" +
                            " fra tidligere søknad med eventDate: ${forrigeBrukersvar.eventDate}"
                )

                val utførtArbeidUtenforNorge =
                    mapBrukersvar.mapUtførtArbeidUtenforNorge(forrigeBrukersvar.utfort_arbeid_utenfor_norge)

                return mapTilBrukerinput(forrigeBrukersvar, utførtArbeidUtenforNorge)
            }
        }
    }

    private fun søknadInneholderNyeBrukerspørsmål(utførtArbeidUtenforNorge: UtfortAarbeidUtenforNorge?): Boolean =
        utførtArbeidUtenforNorge != null

    private fun søknadInneholderGammeltBrukerspørsmålMedSvarJa(brukersvarPåInnkommendeSøknad: Brukersporsmaal?): Boolean =
        brukersvarPåInnkommendeSøknad?.sporsmaal?.arbeidUtland == true


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
            oppholdUtenforEos = mapBrukersvar.mapOppholdUtenforEØS(brukersvar?.oppholdUtenforEOS),
            oppholdUtenforNorge = mapBrukersvar.mapOppholdUtenforNorge(brukersvar?.oppholdUtenforNorge)
        )
    }
}
