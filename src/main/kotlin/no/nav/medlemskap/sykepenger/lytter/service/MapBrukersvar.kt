package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.ArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Opphold
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.OppholdUtenforEos
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.OppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Oppholdstilatelse
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.UtfortAarbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforEØS
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstillatelseBrukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapUtførtArbeidUtenforNorge

object MapBrukersvar {
    fun mapOppholdstillatelse(oppholdstillatelse: MedlemskapOppholdstillatelseBrukerspørsmål?): Oppholdstilatelse? =
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


    fun mapUtførtArbeidUtenforNorge(utfortArbeidUtenforNorge: MedlemskapUtførtArbeidUtenforNorge?): UtfortAarbeidUtenforNorge? =
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

    fun mapOppholdUtenforNorge(oppholdUtenforNorge: MedlemskapOppholdUtenforNorge?): OppholdUtenforNorge? =
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

    fun mapOppholdUtenforEØS(oppholdutenforEØS: MedlemskapOppholdUtenforEØS?): OppholdUtenforEos? =
        oppholdutenforEØS?.let {
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