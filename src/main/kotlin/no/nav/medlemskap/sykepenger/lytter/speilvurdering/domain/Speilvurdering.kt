package no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain

import no.nav.medlemskap.sykepenger.lytter.speilvurdering.Speilsvar

data class Speilvurdering(
    val soknadId: String,
    val fnr: String,
    val speilSvar: Speilsvar,
    val avklaringer: List<String>,
    val kanal: String
)