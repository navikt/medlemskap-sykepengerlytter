package no.nav.medlemskap.sykepenger.lytter.speilvurdering

data class SpeilResponse(
    val soknadId: String,
    val fnr: String,
    val speilSvar: Speilsvar,
    val aarsaker: List<Aarsak> = emptyList()
)

data class Aarsak(
    val regelId: String,
    val forklaringPaaUavklart: String
)

enum class Speilsvar {
    JA,
    NEI,
    UAVKLART,
    UAVKLART_MED_BRUKERSPORSMAAL
}