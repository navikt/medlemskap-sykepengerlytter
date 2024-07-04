package no.nav.medlemskap.sykepenger.lytter.service


import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.*
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.ArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.OppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.persistence.*

fun opprettBrukerInput(brukersporsmaal: Brukersporsmaal, arbeidUtland: Boolean): Brukerinput
{
    val oppholdstilatelse = mapOppholdstilatelse(brukersporsmaal.oppholdstilatelse)
    val oppholdUtenforEOS = mapOppholdUtenforEOS(brukersporsmaal.oppholdUtenforEOS)
    val oppholdUtenforNorge = mapOppholdUtenforNorge(brukersporsmaal.oppholdUtenforNorge)
    val utfortAarbeidUtenforNorge = maputfortAarbeidUtenforNorge(brukersporsmaal.utfort_arbeid_utenfor_norge)
    return Brukerinput(arbeidUtland)
}

fun maputfortAarbeidUtenforNorge(utfortArbeidUtenforNorge: Medlemskap_utfort_arbeid_utenfor_norge?): UtfortAarbeidUtenforNorge? {
    if (utfortArbeidUtenforNorge !=null){
        return UtfortAarbeidUtenforNorge(
            id=utfortArbeidUtenforNorge.id,
            sporsmalstekst = utfortArbeidUtenforNorge.sporsmalstekst,
            svar = utfortArbeidUtenforNorge.svar,
            arbeidUtenforNorge = mapArbeidUtenforNorge(utfortArbeidUtenforNorge.arbeidUtenforNorge)
            )
        }
    return null
}

fun mapArbeidUtenforNorge(arbeidUtenforNorge: List<no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorge>): List<ArbeidUtenforNorge> {
    if (arbeidUtenforNorge.isEmpty()){
        return emptyList()
    }
    return arbeidUtenforNorge.map {
        ArbeidUtenforNorge(
            id = it.id,
            arbeidsgiver = it.arbeidsgiver,
            land  =it.land,
            perioder = it.perioder.map {p->
                Periode(p.fom.toString(),p.tom.toString())
            }
        )
    }
}

fun mapOppholdUtenforNorge(oppholdUtenforNorge: Medlemskap_opphold_utenfor_norge?): OppholdUtenforNorge? {
    if (oppholdUtenforNorge!=null){
        return OppholdUtenforNorge(
            id=oppholdUtenforNorge.id,
            sporsmalstekst = oppholdUtenforNorge.sporsmalstekst,
            svar = oppholdUtenforNorge.svar,
            oppholdUtenforNorge = oppholdUtenforNorge.oppholdUtenforNorge.map {
                Opphold(
                    id = it.id,
                    land = it.land,
                    grunn = it.grunn,
                    perioder = it.perioder.map {p-> Periode(p.fom.toString(),p.tom.toString()) }

                )
            }
        )
    }
    return null
}

fun mapOppholdUtenforEOS(oppholdUtenforEOS: Medlemskap_opphold_utenfor_eos?): OppholdUtenforEos? {
    if (oppholdUtenforEOS!=null){
        return OppholdUtenforEos(
            id=oppholdUtenforEOS.id,
            sporsmalstekst = oppholdUtenforEOS.sporsmalstekst,
            svar = oppholdUtenforEOS.svar,
            oppholdUtenforEOS = oppholdUtenforEOS.oppholdUtenforEOS.map {
                Opphold(
                    id = it.id,
                    land = it.land,
                    grunn = it.grunn,
                    perioder = it.perioder.map {p-> Periode(p.fom.toString(),p.tom.toString()) }

                )
            }
        )
    }
    return null
}


fun mapOppholdstilatelse(oppholdstilatelse: Medlemskap_oppholdstilatelse_brukersporsmaal?): Oppholdstilatelse? {
   if (oppholdstilatelse!=null){
       return Oppholdstilatelse(
           id = oppholdstilatelse.id,
           svar=oppholdstilatelse.svar,
           sporsmalstekst = oppholdstilatelse.sporsmalstekst,
           vedtaksdato = oppholdstilatelse.vedtaksdato,
           vedtaksTypePermanent = oppholdstilatelse.vedtaksTypePermanent,
           perioder = oppholdstilatelse.perioder.map
           { Periode(it.fom.toString(),it.tom.toString()) }.toList())
   }
    return null

}

