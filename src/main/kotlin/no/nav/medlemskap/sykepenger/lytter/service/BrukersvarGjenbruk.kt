package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.UtfortAarbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal

class BrukersvarGjenbruk {

    private val mapBrukersvar = MapBrukersvar()

    fun vurderGjenbrukAvBrukersvar(
        søknadsParametere: SoeknadsParametere,
        persistenceService: PersistenceService
    ): Brukerinput {
        val brukersvarPåSøknad = persistenceService.hentbrukersporsmaalForSoknadID(søknadsParametere.callId)
            ?: return mapTilBrukerinput(arbeidUtenforNorge = false)

        val utførtArbeidUtenforNorge = mapBrukersvar.mapUtførtArbeidUtenforNorge(brukersvarPåSøknad.utfort_arbeid_utenfor_norge)

        return when {
            søknadInneholderNyeBrukerspørsmål(utførtArbeidUtenforNorge) -> mapTilBrukerinput(brukersvarPåSøknad, utførtArbeidUtenforNorge)
            søknadInneholderGammeltBrukerspørsmålMedSvarJa(brukersvarPåSøknad) -> mapTilBrukerinput(arbeidUtenforNorge = true)

            // Søknad inneholder gammelt brukerspørsmål med svar NEI
            else -> {
                val forrigeBrukersvar = finnForrigeBrukersvar(
                        søknadsParametere.fnr,
                        søknadsParametere.førsteDagForYtelse,
                        persistenceService
                    )
                        ?: return mapTilBrukerinput(arbeidUtenforNorge = false)

                val utførtArbeidUtenforNorge =
                    mapBrukersvar.mapUtførtArbeidUtenforNorge(forrigeBrukersvar.utfort_arbeid_utenfor_norge)
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
