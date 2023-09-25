package no.nav.medlemskap.sykepenger.lytter.rest

import java.time.LocalDate

data class FlexRequest(
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val ytelse: BomloYtelse = BomloYtelse.SYKEPENGER,
)
data class FlexVurderingRespons(
    val sykepengesoknad_id: String,
    val vurdering_id: String,
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val status:String
)