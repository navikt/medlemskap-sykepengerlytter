package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest

class BrukersvarGjenbruk {
    fun skalKanskjeGjenbrukes(id: String, persistenceService: PersistenceService): Brukerinput {
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
                val brukerinput = Brukerinput(
                    arbeidUtenforNorge = brukersvar.sporsmaal?.arbeidUtland == true,
                    oppholdstilatelse = null,
                    utfortAarbeidUtenforNorge = null,
                    oppholdUtenforEos = null,
                    oppholdUtenforNorge = null
                )
                return brukerinput
            }
        } else {
            val brukerinput = Brukerinput(
                arbeidUtenforNorge = false,
                oppholdstilatelse = null,
                utfortAarbeidUtenforNorge = null,
                oppholdUtenforEos = null,
                oppholdUtenforNorge = null
            )
            return brukerinput
            //TODO. Bruk FinnForrigeBrukerspørsmaal funksjonen. og kall lovme
        }
    }
}

/*
data class Brukersporsmaal(
    val fnr: String,
    val soknadid: String,
    val eventDate: LocalDate,
    val ytelse: String,
    val status: String,
    val sporsmaal: FlexBrukerSporsmaal?, //fases ut til fordel for nye spørsmål *
    val oppholdstilatelse:Medlemskap_oppholdstilatelse_brukersporsmaal? = null, *
    val utfort_arbeid_utenfor_norge:Medlemskap_utfort_arbeid_utenfor_norge? = null, *
    val oppholdUtenforNorge:Medlemskap_opphold_utenfor_norge? = null,*
    val oppholdUtenforEOS:Medlemskap_opphold_utenfor_eos? = null *

)


data class Brukerinput(
    val arbeidUtenforNorge: Boolean,
    val oppholdstilatelse:Oppholdstilatelse?=null,
    val utfortAarbeidUtenforNorge:UtfortAarbeidUtenforNorge?=null,
    val oppholdUtenforEos:OppholdUtenforEos?=null,
    val oppholdUtenforNorge:OppholdUtenforNorge?=null


data class Oppholdstilatelse(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val vedtaksdato: LocalDate,
    val vedtaksTypePermanent:Boolean,
    val perioder:List<Periode> = mutableListOf()
)

data class Medlemskap_oppholdstilatelse_brukersporsmaal(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val vedtaksdato:LocalDate,
    val vedtaksTypePermanent:Boolean,
    val perioder:List<Periode> = mutableListOf()
)

)*/