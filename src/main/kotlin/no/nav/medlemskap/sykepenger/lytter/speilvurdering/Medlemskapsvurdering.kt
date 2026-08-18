package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import java.util.Date

data class Medlemskapsvurdering(
    val id: String,
    val soknadId: String,
    val date: Date,
    val json: String
)
