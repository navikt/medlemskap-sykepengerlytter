package no.nav.medlemskap.sykepenger.lytter.domain

import java.time.LocalDate
import java.time.LocalDateTime

data class SykepengeSoknad(
    val id: String?,
    val type: String?,
    val status: String?,
    val fnr: String?,
    val sykmeldingId: String? = null,
    val arbeidssituasjon: String? = null,
    val korrigerer: String? = null,
    val korrigertAv: String? = null,
    val soktUtenlandsopphold: Boolean? = null,
    val fom: String? = null,
    val tom: String? = null,
    val dodsdato: String? = null,
    val startSyketilfelle: String? = null,
    val arbeidGjenopptatt: String? = null,
    val sykmeldingSkrevet: String? = null,
    val opprettet: String? = null,
    val sendtNav: String? = null,
    val sendtArbeidsgiver: String? = null,
    //val sporsmal: List<SporsmalDTO>? = null,

    val ettersending: Boolean? = false,

    val egenmeldtSykmelding: Boolean? = null,
    val harRedusertVenteperiode: Boolean? = null,
    val behandlingsdager: List<LocalDate>? = null,

)
