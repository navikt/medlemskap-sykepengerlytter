package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.saga.persistence.*
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import java.time.LocalDate

fun finnAlleredeStilteBrukerSpørsmålArbeidUtland(brukersporsmaal: List<Brukersporsmaal>) : Medlemskap_utfort_arbeid_utenfor_norge?{
     val arbeidUtenForNorge = brukersporsmaal.associate { Pair(it.eventDate,it.utfort_arbeid_utenfor_norge) }.filter { it.value!=null }
    if (arbeidUtenForNorge.isEmpty()){
        return null
    }
    val datoSisteBrukerspørsmålStilt = arbeidUtenForNorge.toSortedMap().lastKey()
    val sistOppgitteArbeidUtenforNorgeBrukersporsmaal = arbeidUtenForNorge[datoSisteBrukerspørsmålStilt]

    if (!sistOppgitteArbeidUtenforNorgeBrukersporsmaal!!.svar){
        if (datoSisteBrukerspørsmålStilt.isAfter(LocalDate.now().minusDays(32))){
            return sistOppgitteArbeidUtenforNorgeBrukersporsmaal
        }
    }
    return null
}

fun finnAlleredeStilteBrukerSpørsmålOppholdUtenforNorge(brukersporsmaal: List<Brukersporsmaal>) : Medlemskap_opphold_utenfor_norge?{
    val opphold_utenfor_norge = brukersporsmaal.associate { Pair(it.eventDate,it.oppholdUtenforNorge) }.filter { it.value!=null }
    if (opphold_utenfor_norge.isEmpty()){
        return null
    }
    val datoSisteBrukerspørsmålStilt = opphold_utenfor_norge.toSortedMap().lastKey()
    val sistOppgitteOppholdUtenforNorgeBrukersporsmaal = opphold_utenfor_norge[datoSisteBrukerspørsmålStilt]
    if (!sistOppgitteOppholdUtenforNorgeBrukersporsmaal!!.svar){
        if (datoSisteBrukerspørsmålStilt.isAfter(LocalDate.now().minusMonths(5).minusDays(15))){
            return sistOppgitteOppholdUtenforNorgeBrukersporsmaal
        }
    }
    return null

}
fun finnAlleredeStilteBrukerSpørsmålOppholdUtenforEOS(brukersporsmaal: List<Brukersporsmaal>) : Medlemskap_opphold_utenfor_eos?{
    val opphold_utenfor_eos = brukersporsmaal.associate { Pair(it.eventDate,it.oppholdUtenforEOS) }.filter { it.value!=null }
    if (opphold_utenfor_eos.isEmpty()){
        return null
    }
    val datoSisteBrukerspørsmålStilt = opphold_utenfor_eos.toSortedMap().lastKey()
    val sistOppgitteOppholdUtenforEOSBrukersporsmaal = opphold_utenfor_eos[datoSisteBrukerspørsmålStilt]
    if (!sistOppgitteOppholdUtenforEOSBrukersporsmaal!!.svar){
        if (datoSisteBrukerspørsmålStilt.isAfter(LocalDate.now().minusMonths(5).minusDays(15))){
            return sistOppgitteOppholdUtenforEOSBrukersporsmaal
        }
    }
    return null
}
fun finnAlleredeStilteBrukerSpørsmåloppholdstilatelse(brukersporsmaal: List<Brukersporsmaal>) : Medlemskap_oppholdstilatelse_brukersporsmaal?{
    val oppholdstilatelse_brukersporsmaal = brukersporsmaal.associate { Pair(it.eventDate,it.oppholdstilatelse) }.filter { it.value!=null }
    if (oppholdstilatelse_brukersporsmaal.isEmpty()){
        return null
    }
    val datoSisteBrukerspørsmålStilt = oppholdstilatelse_brukersporsmaal.toSortedMap().lastKey()
    val sistOppgitteOpphldstilatelseBrukersporsmaal = oppholdstilatelse_brukersporsmaal[datoSisteBrukerspørsmålStilt]
    if (!sistOppgitteOpphldstilatelseBrukersporsmaal!!.svar){
        return null //her skal vi tvinge frem nye bruker spørsmål. kommer dog trolig aldri til å skje
    }
    if (sistOppgitteOpphldstilatelseBrukersporsmaal.vedtaksTypePermanent){
        if (datoSisteBrukerspørsmålStilt.isBefore(LocalDate.now().minusYears(1))){
            return null
        }
        return sistOppgitteOpphldstilatelseBrukersporsmaal

    }
    else if (!sistOppgitteOpphldstilatelseBrukersporsmaal.vedtaksTypePermanent){
        if (sistOppgitteOpphldstilatelseBrukersporsmaal.perioder.first().erAvsluttetPr(LocalDate.now())){
            return null
        }
        else{
            return sistOppgitteOpphldstilatelseBrukersporsmaal
        }
    }
    return null
}

fun createFlexRespons(suggestedRespons: FlexRespons, alleredeStilteSporsmaal: List<Spørsmål>): FlexRespons {

    val questions = suggestedRespons.sporsmal.filter { !alleredeStilteSporsmaal.contains(it) }
    return FlexRespons(
        svar = suggestedRespons.svar,
        sporsmal = questions.toSet()
    )

}