package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import java.time.LocalDate

data class Medlemskapsstatus(
    val sykepengesoknad_id: String,
    val vurdering_id: String,
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val status: Status
)
