package no.nav.medlemskap.sykepenger.lytter.rest

import java.time.LocalDate

data class BomloRequest(
    val fnr: String,
    val førsteDagForYtelse: LocalDate?,
    val periode: BomloInputPeriode,
    val ytelse: BomloYtelse?,
)

data class BomloInputPeriode(
    val fom: LocalDate,
    val tom: LocalDate
)
enum class BomloYtelse {
    SYKEPENGER
}