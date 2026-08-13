package no.nav.medlemskap.sykepenger.lytter.domain

import java.time.LocalDate

data class Medlemskap(val fnr: String,val fom: LocalDate,val tom: LocalDate,val medlem: Status) {

}
enum class Status {
    JA,
    NEI,
    UAVKLART,
    PAFOLGENDE,
}
