package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import java.time.LocalDate

data class MedlemskapsstatusRequest(
    val sykepengesoknad_id: String,
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val ytelse: Ytelse = Ytelse.SYKEPENGER,
)
