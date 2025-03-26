package no.nav.medlemskap.sykepenger.lytter.domain

import com.fasterxml.jackson.annotation.JsonCreator
import java.time.LocalDate
import java.time.LocalDateTime

data class LovmeSoknadDTO(
    val id: String,
    val type: SoknadstypeDTO,
    val status: String,
    val fnr: String,
    val korrigerer: String? = null,
    val startSyketilfelle: LocalDate?,
    val sendtNav: LocalDateTime?,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val ettersending:Boolean? = null,
    val forstegangssoknad: Boolean? = null,
    // Kun True eller False hvis bruker har svar JA eller NEI.
    val arbeidUtenforNorge: Boolean? = null)

enum class SoknadsstatusDTO {
    NY,
    SENDT,
    FREMTIDIG,
    KORRIGERT,
    AVBRUTT,
    SLETTET
}
enum class SoknadstypeDTO {
    SELVSTENDIGE_OG_FRILANSERE,
    OPPHOLD_UTLAND,
    ARBEIDSTAKERE,
    ANNET_ARBEIDSFORHOLD,
    ARBEIDSLEDIG,
    BEHANDLINGSDAGER,
    REISETILSKUDD,
    GRADERT_REISETILSKUDD,
    FRISKMELDT_TIL_ARBEIDSFORMIDLING,
    UKJENT(); // Default-verdi

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String?): SoknadstypeDTO {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UKJENT
        }
    }
}