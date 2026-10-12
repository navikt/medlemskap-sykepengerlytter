package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import java.time.LocalDate

data class VurderingRequest(
    val sykepengesoknad_id: String,
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val ytelse: Ytelse = Ytelse.SYKEPENGER,
)

fun medlemskapsstatusRequestTilVurderingRequest(medlemskapsstatusRequest: MedlemskapsstatusRequest): VurderingRequest =
    VurderingRequest(
        sykepengesoknad_id = medlemskapsstatusRequest.sykepengesoknad_id,
        fnr = medlemskapsstatusRequest.fnr,
        fom = medlemskapsstatusRequest.fom,
        tom = medlemskapsstatusRequest.tom,
        ytelse = medlemskapsstatusRequest.ytelse,
    )
