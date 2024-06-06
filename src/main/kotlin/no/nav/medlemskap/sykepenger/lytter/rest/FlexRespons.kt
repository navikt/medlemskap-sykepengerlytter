package no.nav.medlemskap.sykepenger.lytter.rest


import java.time.LocalDate

data class FlexRespons(
    val svar:Svar,
    val sporsmal:Set<Spørsmål>,
    var kjentOppholdstillatelse: Periode? = null
)

data class Periode(val fom: LocalDate,val tom:LocalDate?)
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