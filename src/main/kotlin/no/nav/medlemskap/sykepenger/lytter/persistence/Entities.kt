package no.nav.medlemskap.saga.persistence

import java.time.LocalDate

data class VurderingDao(val id:String, val fnr: String, val fom: LocalDate, val tom: LocalDate, val status: String)
