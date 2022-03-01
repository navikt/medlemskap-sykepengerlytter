package no.nav.medlemskap.sykepenger.lytter.domain

import java.time.LocalDate

data class Medlemskap(val fnr:String,val fom:LocalDate,val tom:LocalDate,val ytelse:String,val medlem:ErMedlem) {

}
data class PersonRecord(val fnr:String,val vurderinger: Array<Medlemskap>)
enum class ErMedlem{
    JA,
    NEI,
    UAVKLART,
}

fun Medlemskap.erpåfølgende(medlemskap: Medlemskap):Boolean{
    return fom.isEqual(medlemskap.tom.plusDays(1)) ||
            erIMidtenAv(medlemskap)
}
private fun Medlemskap.erIMidtenAv(medlemskap: Medlemskap):Boolean{
    return  (fom.isAfter(medlemskap.fom) && fom.isBefore(medlemskap.tom))
}

