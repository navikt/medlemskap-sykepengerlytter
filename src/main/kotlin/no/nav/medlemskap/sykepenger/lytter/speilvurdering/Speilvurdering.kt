package no.nav.medlemskap.sykepenger.lytter.speilvurdering

data class Speilvurdering(
    val soknadId: String,
    val fnr: String,
    val speilSvar: Speilsvar,
    val avklaringer: List<String>,
    val kanal: String
)
