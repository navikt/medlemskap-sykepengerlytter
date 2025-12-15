package no.nav.medlemskap.sykepenger.lytter.service


import no.nav.medlemskap.sykepenger.lytter.persistence.*
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
    val datoForNyModell = LocalDate.of(2024,4,23)
    val oppholdstilatelse_brukersporsmaal = brukersporsmaal.associate { Pair(it.eventDate,it.oppholdstilatelse) }.filter { it.value!=null && it.key.isAfter(datoForNyModell) }
    if (oppholdstilatelse_brukersporsmaal.isEmpty()){
        return null
    }
    val datoSisteBrukerspørsmålStilt = oppholdstilatelse_brukersporsmaal.toSortedMap().lastKey()
    val sistOppgitteOpphldstilatelseBrukersporsmaal = oppholdstilatelse_brukersporsmaal[datoSisteBrukerspørsmålStilt]
    if (!sistOppgitteOpphldstilatelseBrukersporsmaal!!.svar){
        return null //her skal vi tvinge frem nye bruker spørsmål. kommer dog trolig aldri til å skje
    }

    return sistOppgitteOpphldstilatelseBrukersporsmaal
}

/*
* SP1160
* */
fun finnAlleredeStilteBrukerSprøsmål(alleBrukerSpormaalForBruker: List<Brukersporsmaal>): List<Spørsmål> {
    val alleredespurteBrukersporsmaal = mutableListOf<Spørsmål>()
    val arbeidUtland = finnAlleredeStilteBrukerSpørsmålArbeidUtland(alleBrukerSpormaalForBruker)
    val oppholdUtenforEOS = finnAlleredeStilteBrukerSpørsmålOppholdUtenforEOS(alleBrukerSpormaalForBruker)
    val oppholdUtenforNorge = finnAlleredeStilteBrukerSpørsmålOppholdUtenforNorge(alleBrukerSpormaalForBruker)
    val oppholdstilatesle = finnAlleredeStilteBrukerSpørsmåloppholdstilatelse(alleBrukerSpormaalForBruker)
    if (arbeidUtland!=null){
        alleredespurteBrukersporsmaal.add(Spørsmål.ARBEID_UTENFOR_NORGE)
    }
    if (oppholdUtenforEOS!=null){
        alleredespurteBrukersporsmaal.add(Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
    }
    if (oppholdUtenforNorge!=null){
        alleredespurteBrukersporsmaal.add(Spørsmål.OPPHOLD_UTENFOR_NORGE)
    }
    if (oppholdstilatesle!=null){
        alleredespurteBrukersporsmaal.add(Spørsmål.OPPHOLDSTILATELSE)
    }
    return alleredespurteBrukersporsmaal
}
