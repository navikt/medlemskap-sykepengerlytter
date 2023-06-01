package no.nav.medlemskap.sykepenger.lytter.rest

data class FlexRespons(
  val svar:Svar,
  val sporsmal:Set<Spørsmål>
)

enum class Spørsmål{
    OPPHOLDSTILATELSE,
    ARBEID_UTENFOR_NORGE,
    OPPHOLD_UTENFOR_EØS_OMRÅDE,
    OPPHOLD_UTENFOR_NORGE
}
enum class Svar{
    JA,
    NEI,
    UAVKLART
}