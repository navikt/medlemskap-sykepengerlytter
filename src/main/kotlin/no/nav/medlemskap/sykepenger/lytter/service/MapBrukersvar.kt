package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.ArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Opphold
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.OppholdUtenforEos
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.OppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Oppholdstilatelse
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.UtfortAarbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforEOS
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstillatelseBrukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapUtførtArbeidUtenforNorge

object MapBrukersvar {
    fun mapOppholdstillatelse(oppholdstillatelse: MedlemskapOppholdstillatelseBrukerspørsmål?): Oppholdstilatelse? =
        oppholdstillatelse?.let {
            Oppholdstilatelse(
                id = it.id,
                sporsmalstekst = it.spørsmalstekst,
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
                sporsmalstekst = it.spørsmålstekst,
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

    fun mapOppholdUtenforEØS(oppholdutenforEØS: MedlemskapOppholdUtenforEOS?): OppholdUtenforEos? =
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