package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.ArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Opphold
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.OppholdUtenforEos
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.OppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Oppholdstilatelse
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.UtfortAarbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_eos
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_norge
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_oppholdstilatelse_brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_utfort_arbeid_utenfor_norge

class MapBrukersvar {
    fun mapOppholdstillatelse(oppholdstillatelse: Medlemskap_oppholdstilatelse_brukersporsmaal?): Oppholdstilatelse? =
        oppholdstillatelse?.let {
            Oppholdstilatelse(
                id = it.id,
                sporsmalstekst = it.sporsmalstekst,
                svar = it.svar,
                vedtaksdato = it.vedtaksdato,
                vedtaksTypePermanent = it.vedtaksTypePermanent,
                perioder = it.perioder.map { periode ->
                    Periode(periode.fom.toString(), periode.tom.toString())
                }
            )
        }


    fun mapUtførtArbeidUtenforNorge(utfortArbeidUtenforNorge: Medlemskap_utfort_arbeid_utenfor_norge?): UtfortAarbeidUtenforNorge? =
        utfortArbeidUtenforNorge?.let {
            UtfortAarbeidUtenforNorge(
                id = it.id,
                sporsmalstekst = it.sporsmalstekst,
                svar = it.svar,
                arbeidUtenforNorge = it.arbeidUtenforNorge.map { arbeidUtenforNorge ->
                    ArbeidUtenforNorge(
                        arbeidUtenforNorge.id,
                        arbeidsgiver = arbeidUtenforNorge.arbeidsgiver,
                        land = arbeidUtenforNorge.land,
                        perioder = arbeidUtenforNorge.perioder.map { periode ->
                            Periode(periode.fom.toString(), periode.tom.toString())
                        }
                    )
                }
            )
        }

    fun mapOppholdUtenforNorge(oppholdUtenforNorge: Medlemskap_opphold_utenfor_norge?): OppholdUtenforNorge? =
        oppholdUtenforNorge?.let {
            OppholdUtenforNorge(
                id = it.id,
                sporsmalstekst = it.sporsmalstekst,
                svar = it.svar,
                oppholdUtenforNorge = it.oppholdUtenforNorge.map { oppholdUtenforNorge ->
                    Opphold(
                        oppholdUtenforNorge.id,
                        land = oppholdUtenforNorge.land,
                        grunn = oppholdUtenforNorge.grunn,
                        perioder = oppholdUtenforNorge.perioder.map { periode ->
                            Periode(periode.fom.toString(), periode.tom.toString())
                        }
                    )
                }
            )
        }

    fun mapOppholdUtenforEos(oppholdutenforEos: Medlemskap_opphold_utenfor_eos?): OppholdUtenforEos? =
        oppholdutenforEos?.let {
            OppholdUtenforEos(
                id = it.id,
                sporsmalstekst = it.sporsmalstekst,
                svar = it.svar,
                oppholdUtenforEOS = it.oppholdUtenforEOS.map { oppholdUtenforEOS ->
                    Opphold(
                        id = oppholdUtenforEOS.id,
                        land = oppholdUtenforEOS.land,
                        grunn = oppholdUtenforEOS.grunn,
                        perioder = oppholdUtenforEOS.perioder.map { periode ->
                            Periode(periode.fom.toString(), periode.tom.toString())
                        }
                    )
                }
            )
        }

    fun kopierFraUtførtArbeidUtenforNorge(svar: Boolean): Boolean {
        return svar
    }

}