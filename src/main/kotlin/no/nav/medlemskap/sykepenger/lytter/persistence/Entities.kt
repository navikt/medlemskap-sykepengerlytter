package no.nav.medlemskap.saga.persistence


import java.time.LocalDate

data class VurderingDao(val id:String, val fnr: String, val fom: LocalDate, val tom: LocalDate, val status: String)

data class Brukersporsmaal(
    val fnr:String,
    val soknadid: String,
    val eventDate: LocalDate,
    val ytelse: String,
    val status: String,
    val sporsmaal: FlexBrukerSporsmaal?)

data class FlexBrukerSporsmaal(
    val arbeidUtland:Boolean?
)



