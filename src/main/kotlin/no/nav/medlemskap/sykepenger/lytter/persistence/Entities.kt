package no.nav.medlemskap.saga.persistence

import java.time.LocalDate

data class VurderingDao(val fnr: String, val fom: LocalDate, val tom: LocalDate, val status: String)
