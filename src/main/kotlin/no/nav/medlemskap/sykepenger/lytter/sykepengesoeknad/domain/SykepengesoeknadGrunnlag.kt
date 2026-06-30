package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class SykepengesoeknadGrunnlag(
    val id: String,
    val type: Type,
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