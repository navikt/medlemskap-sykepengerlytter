package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.saga.persistence.*
import java.time.LocalDate

fun finnMedlemskap_utfort_arbeid_utenfor_norge(listofbrukersporsmaal: List<Brukersporsmaal>): Medlemskap_utfort_arbeid_utenfor_norge? {

    //for arbeid utenfor norge, skal 5 mnd brukes dersom man ikk har oppgitt arbeid utland siste 5 mnd.
    //ellers skal 32 dager benyttes

    val mapOfutfortarbeid = listofbrukersporsmaal.associate { Pair(it.eventDate,it.utfort_arbeid_utenfor_norge) }
    val arbeidUtland = mapOfutfortarbeid.filter { it.value?.svar ==true}
    val ikkeArbeidUtland = mapOfutfortarbeid.filter { it.value?.svar ==false}
    if (arbeidUtland.isNotEmpty()){
        val aktuelle = arbeidUtland.filter { it.key.isAfter(LocalDate.now().minusDays(32)) }
        if (aktuelle.isNotEmpty()){
            return aktuelle.map { it.value }.first()
        }
        else return null //TODO: Verifiser at dette er funksjonelt korrekt!
    }
    if (ikkeArbeidUtland.isNotEmpty()){
        val aktuelle = arbeidUtland.filter { it.key.isAfter(LocalDate.now().minusMonths(5)) }
        if (aktuelle.isNotEmpty()){
            return aktuelle.toSortedMap().map { it.value }.last()
        }
    }
    return null
}
fun finnMedlemskap_opphold_utenfor_norge(listofbrukersporsmaal: List<Brukersporsmaal>): Medlemskap_opphold_utenfor_norge? {
    return null
}
fun finnMedlemskap_opphold_utenfor_eos(listofbrukersporsmaal: List<Brukersporsmaal>): Medlemskap_opphold_utenfor_eos? {
    return null
}
fun finnMMedlemskap_oppholdstilatelse_brukersporsmaal(listofbrukersporsmaal: List<Brukersporsmaal>): Medlemskap_oppholdstilatelse_brukersporsmaal? {
    val listofMedlemskap_oppholdstilatelse_brukersporsmaal = listofbrukersporsmaal.map { it.oppholdstilatelse }
    val aktuelleOppgoldstilatelser = listofMedlemskap_oppholdstilatelse_brukersporsmaal.filter { it?.vedtaksTypePermanent ==true || (it?.perioder?.isNotEmpty() ==true &&  it?.perioder?.first()?.erAvsluttetPr(LocalDate.now()) != true) }
    if (aktuelleOppgoldstilatelser.isEmpty()){
        return null
    }
    val vedtakdato = aktuelleOppgoldstilatelser.sortedBy { it?.vedtaksdato }.last()
    val permanent = aktuelleOppgoldstilatelser.filter { it?.vedtaksTypePermanent==true }
    if (permanent.isNotEmpty()){
        return permanent.sortedBy { it?.vedtaksdato }.last()
    }
    return vedtakdato

}