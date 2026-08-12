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
