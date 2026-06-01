package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

data class SpeilRespons(val soknadId: String, val fnr: String, val speilSvar: Speilsvar)

enum class Speilsvar {
    JA,
    NEI,
    UAVKLART,
    UAVKLART_MED_BRUKERSPORSMAAL,
}
