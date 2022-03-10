package no.nav.medlemskap.sykepenger.lytter.domain

import java.time.LocalDate

data class Medlemskap(val fnr:String,val fom:LocalDate,val tom:LocalDate,val medlem:ErMedlem) {

}
enum class ErMedlem{
    JA,
    NEI,
    UAVKLART,
    PAFOLGENDE,
}

fun Medlemskap.erpåfølgende(medlemskap: Medlemskap):Boolean{
    return fom.isEqual(medlemskap.tom.plusDays(1)) ||
            erIMidtenAv(medlemskap)
}
fun Medlemskap.erFunkskjoneltLik(medlemskap: Medlemskap):Boolean{
    return fom.isEqual(medlemskap.fom) && tom.isEqual(medlemskap.tom)
}
private fun Medlemskap.erIMidtenAv(medlemskap: Medlemskap):Boolean{
    return  (fom.isAfter(medlemskap.fom) && fom.isBefore(medlemskap.tom))
}

