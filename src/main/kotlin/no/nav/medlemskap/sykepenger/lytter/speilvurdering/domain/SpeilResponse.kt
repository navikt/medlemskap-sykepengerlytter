package no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain

data class SpeilResponse(val soknadId: String, val fnr: String, val speilSvar: Speilsvar)

enum class Speilsvar{
    JA,
    NEI,
    UAVKLART,
    UAVKLART_MED_BRUKERSPORSMAAL
}