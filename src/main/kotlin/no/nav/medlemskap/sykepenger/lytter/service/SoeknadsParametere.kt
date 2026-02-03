package no.nav.medlemskap.sykepenger.lytter.service

data class SoeknadsParametere(
    val callId: String,
    val fnr: String,
    val førsteDagForYtelse: String
)