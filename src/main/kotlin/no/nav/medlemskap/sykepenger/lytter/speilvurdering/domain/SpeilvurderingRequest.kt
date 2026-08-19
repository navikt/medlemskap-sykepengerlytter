package no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain

import java.time.LocalDate

data class SpeilvurderingRequest(
    val fnr: String,
    val førsteDagForYtelse: LocalDate?,
    val periode: Periode,
    val ytelse: Ytelse?,
)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate
)
enum class Ytelse {
    SYKEPENGER
}