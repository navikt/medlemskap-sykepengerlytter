package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest

class BrukersvarGjenbruk {
    fun skalKanskjeGjenbrukes(
        id: String,
        fnr: String,
        førsteDagForYtelse: String,
        persistenceService: PersistenceService
    ): Brukerinput {
        val brukersvar = persistenceService.hentbrukersporsmaalForSoknadID(id)
        if (brukersvar != null) {
            val mapBrukersvar = MapBrukersvar()
            val utførtArbeidUtenforNorge =
                mapBrukersvar.mapUtførtArbeidUtenforNorge(brukersvar.utfort_arbeid_utenfor_norge)

            if (utførtArbeidUtenforNorge != null) {
                val brukerinput = Brukerinput(
                    arbeidUtenforNorge = mapBrukersvar.kopierFraUtførtArbeidUtenforNorge(utførtArbeidUtenforNorge.svar),
                    oppholdstilatelse = mapBrukersvar.mapOppholdstillatelse(brukersvar.oppholdstilatelse),
                    utfortAarbeidUtenforNorge = utførtArbeidUtenforNorge,
                    oppholdUtenforEos = mapBrukersvar.mapOppholdUtenforEos(brukersvar.oppholdUtenforEOS),
                    oppholdUtenforNorge = mapBrukersvar.mapOppholdUtenforNorge(brukersvar.oppholdUtenforNorge)
                )
                return brukerinput
            } else {
                //Søknad inneholder gammelt brukerspørsmål.
                val svar = brukersvar.sporsmaal?.arbeidUtland
                if (svar == true) {
                    val brukerinput = Brukerinput(
                        arbeidUtenforNorge = brukersvar.sporsmaal.arbeidUtland,
                        oppholdstilatelse = null,
                        utfortAarbeidUtenforNorge = null,
                        oppholdUtenforEos = null,
                        oppholdUtenforNorge = null
                    )
                    return brukerinput
                } else {
                    val forrigeBrukersvar =
                        finnForrigeBrukersvar(fnr, førsteDagForYtelse, persistenceService = persistenceService)

                    if (forrigeBrukersvar != null) {
                        val mapBrukersvar = MapBrukersvar()
                        val utførtArbeidUtenforNorge =
                            mapBrukersvar.mapUtførtArbeidUtenforNorge(forrigeBrukersvar.utfort_arbeid_utenfor_norge)
                        return Brukerinput(
                            arbeidUtenforNorge = mapBrukersvar.kopierFraUtførtArbeidUtenforNorge(
                                utførtArbeidUtenforNorge?.svar
                                    ?: false
                            ),
                            oppholdstilatelse = mapBrukersvar.mapOppholdstillatelse(forrigeBrukersvar.oppholdstilatelse),
                            utfortAarbeidUtenforNorge = utførtArbeidUtenforNorge,
                            oppholdUtenforEos = mapBrukersvar.mapOppholdUtenforEos(forrigeBrukersvar.oppholdUtenforEOS),
                            oppholdUtenforNorge = mapBrukersvar.mapOppholdUtenforNorge(forrigeBrukersvar.oppholdUtenforNorge)
                        )

                    } else {
                        return Brukerinput(
                            arbeidUtenforNorge = false,
                            oppholdstilatelse = null,
                            utfortAarbeidUtenforNorge = null,
                            oppholdUtenforEos = null,
                            oppholdUtenforNorge = null
                        )
                    }

                }


            }

        }
        return Brukerinput(
            arbeidUtenforNorge = false,
            oppholdstilatelse = null,
            utfortAarbeidUtenforNorge = null,
            oppholdUtenforEos = null,
            oppholdUtenforNorge = null
        )
    }
}