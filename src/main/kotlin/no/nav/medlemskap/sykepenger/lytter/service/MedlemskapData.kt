package no.nav.medlemskap.sykepenger.lytter.domain

import java.time.LocalDate

data class MedlemskapVurdering(
    val resultat: Resultat,
    val datagrunnlag: Datagrunnlag?
)

data class Resultat(
    val svar: String,
    val delresultat: List<Delresultat>,
    val årsaker: List<Årsak>
)

data class Delresultat(
    val regelId: String,
    val svar: String,
    val delresultat: List<Delresultat>?
)

data class Årsak(
    val regelId: String
)

data class Datagrunnlag(
    val oppholdstillatelse: Oppholdstillatelse?
)

data class Oppholdstillatelse(
    val gjeldendeOppholdsstatus: GjeldendeOppholdsstatus?
)

data class GjeldendeOppholdsstatus(
    val oppholdstillatelsePaSammeVilkar: OppholdstillatelsePaSammeVilkar?
)

data class OppholdstillatelsePaSammeVilkar(
    val periode: OppholdstillatelsePeriode?
)

data class OppholdstillatelsePeriode(
    val fom: LocalDate?,
    val tom: LocalDate?
)