package no.nav.medlemskap.sykepenger.lytter.domain

import java.time.LocalDate

data class Vurderingsstatus(
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val status: Status
)

enum class Status {
    JA,
    NEI,
    UAVKLART,
    PAFOLGENDE,
}
