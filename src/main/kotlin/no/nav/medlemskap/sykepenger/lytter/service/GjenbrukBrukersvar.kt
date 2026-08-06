package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import org.slf4j.MarkerFactory

class GjenbrukBrukersvar(private val tidligereBrukersvar: TidligereBrukersvar) {

    private val log = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    private val mapBrukersvar: MapBrukersvar = MapBrukersvar

    fun fraInnkommendeSøknad(
        søknadsParametere: SoeknadsParametere,
        brukersvarPåInnkommendeSøknad: Brukersporsmaal?
    ): Brukerinput =
        GjenbrukKontekst(søknadsParametere, brukersvarPåInnkommendeSøknad, Kilde.SYKEPENGEBACKEND)
            .vurderBrukersvar()
            .tilLoggetBrukerinput()

    fun fraTidligereSvar(
        søknadsParametere: SoeknadsParametere,
        kilde: Kilde
    ): Brukerinput =
        GjenbrukKontekst(søknadsParametere, brukersvarPåInnkommendeSøknad = null, kilde)
            .finnTidligereBrukersvar()
            .tilLoggetBrukerinput()

    private fun GjenbrukKontekst.vurderBrukersvar(): GjenbrukResultat =
        vurderInnkommendeBrukersvar() ?: finnTidligereBrukersvar()

    private fun GjenbrukKontekst.vurderInnkommendeBrukersvar(): GjenbrukResultat? {
        val innkommendeBrukersvar = brukersvarPåInnkommendeSøknad

        return when {
            innkommendeBrukersvar != null && søknadInneholderNyeBrukerspørsmål(innkommendeBrukersvar) ->
                GjenbrukResultat.NyeBrukerspørsmål(søknadsParametere, innkommendeBrukersvar)

            søknadInneholderGammeltBrukerspørsmålMedSvarJa(innkommendeBrukersvar) ->
                GjenbrukResultat.ArbeidUtenforNorgeSvarJa(søknadsParametere)

            else -> null
        }
    }

    private fun GjenbrukKontekst.finnTidligereBrukersvar(): GjenbrukResultat {
        loggVurdererGjenbrukAvTidligereSvar()

        val forrigeBrukersvar = tidligereBrukersvar.finnNyesteGjenbrukbareSvar(
            søknadsParametere.fnr,
            søknadsParametere.førsteDagForYtelse
        )

        return if (forrigeBrukersvar == null) {
            GjenbrukResultat.DefaultArbeidUtenforNorgeSvarNei(søknadsParametere)
        } else {
            GjenbrukResultat.TidligereBrukersvar(søknadsParametere, forrigeBrukersvar)
        }
    }

    private fun GjenbrukKontekst.loggVurdererGjenbrukAvTidligereSvar() {
        when (kilde) {
            Kilde.SPEIL ->
                log.info(
                    teamLogs,
                    "Vurderer gjenbruk av tidligere brukersvar for forespørsel fra Speil for person: ${søknadsParametere.fnr}"
                )

            Kilde.SYKEPENGEBACKEND ->
                log.info(
                    teamLogs,
                    "Vurderer gjenbruk av tidligere brukersvar for forespørsel fra Sykepengebackend for person: ${søknadsParametere.fnr}"
                )
        }
    }

    private fun GjenbrukResultat.loggResultat(): GjenbrukResultat =
        also {
            when (this) {
                is GjenbrukResultat.NyeBrukerspørsmål ->
                    log.info(
                        teamLogs,
                        "Søknad med callId: ${søknadsParametere.callId} for person: ${søknadsParametere.fnr} inneholder nye brukerspørsmål." +
                                "Ingen gjenbruk av tidligere brukersvar vil bli gjort"
                    )

                is GjenbrukResultat.ArbeidUtenforNorgeSvarJa ->
                    log.info(
                        teamLogs,
                        "Søknad med callId: ${søknadsParametere.callId} for person ${søknadsParametere.fnr} inneholder gammelt brukerspørsmål med svar JA" +
                                "Ingen gjenbruk av tidligere brukersvar vil bli gjort"
                    )

                is GjenbrukResultat.DefaultArbeidUtenforNorgeSvarNei ->
                    log.info(
                        teamLogs,
                        "Ingen gjenbrukbare brukersvar funnet for person: ${søknadsParametere.fnr}. Setter gammelt brukerspørsmål til standardverdi NEI"
                    )

                is GjenbrukResultat.TidligereBrukersvar ->
                    log.info(
                        teamLogs,
                        "Fant gjenbrukbart brukersvar. Gjenbruker brukersvaret funnet for person: ${søknadsParametere.fnr}" +
                                " fra tidligere søknad med eventDate: ${brukersvar.eventDate}"
                    )
            }
        }

    private fun GjenbrukResultat.tilLoggetBrukerinput(): Brukerinput =
        loggResultat().tilBrukerinput()

    private fun GjenbrukResultat.tilBrukerinput(): Brukerinput =
        when (this) {
            is GjenbrukResultat.NyeBrukerspørsmål -> mapTilBrukerinput(brukersvar)
            is GjenbrukResultat.ArbeidUtenforNorgeSvarJa -> mapTilBrukerinput(arbeidUtenforNorge = true)
            is GjenbrukResultat.DefaultArbeidUtenforNorgeSvarNei -> mapTilBrukerinput(arbeidUtenforNorge = false)
            is GjenbrukResultat.TidligereBrukersvar -> mapTilBrukerinput(brukersvar)
        }

    private fun søknadInneholderNyeBrukerspørsmål(brukersvar: Brukersporsmaal?): Boolean =
        brukersvar?.utfort_arbeid_utenfor_norge != null ||
                brukersvar?.oppholdstilatelse != null ||
                brukersvar?.oppholdUtenforEOS != null ||
                brukersvar?.oppholdUtenforNorge != null

    private fun søknadInneholderGammeltBrukerspørsmålMedSvarJa(brukersvarPåInnkommendeSøknad: Brukersporsmaal?): Boolean =
        brukersvarPåInnkommendeSøknad?.sporsmaal?.arbeidUtland == true

    private fun mapTilBrukerinput(arbeidUtenforNorge: Boolean): Brukerinput =
        Brukerinput(arbeidUtenforNorge = arbeidUtenforNorge)

    private fun mapTilBrukerinput(brukersvar: Brukersporsmaal?): Brukerinput {
        val utførtArbeidUtenforNorge =
            mapBrukersvar.mapUtførtArbeidUtenforNorge(brukersvar?.utfort_arbeid_utenfor_norge)
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

private data class GjenbrukKontekst(
    val søknadsParametere: SoeknadsParametere,
    val brukersvarPåInnkommendeSøknad: Brukersporsmaal?,
    val kilde: Kilde
)

enum class Kilde {
    SPEIL,
    SYKEPENGEBACKEND
}

private sealed interface GjenbrukResultat {
    val søknadsParametere: SoeknadsParametere

    data class NyeBrukerspørsmål(
        override val søknadsParametere: SoeknadsParametere,
        val brukersvar: Brukersporsmaal
    ) : GjenbrukResultat

    data class ArbeidUtenforNorgeSvarJa(
        override val søknadsParametere: SoeknadsParametere
    ) : GjenbrukResultat

    data class DefaultArbeidUtenforNorgeSvarNei(
        override val søknadsParametere: SoeknadsParametere
    ) : GjenbrukResultat

    data class TidligereBrukersvar(
        override val søknadsParametere: SoeknadsParametere,
        val brukersvar: Brukersporsmaal
    ) : GjenbrukResultat
}
