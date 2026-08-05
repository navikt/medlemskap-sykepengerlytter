package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapVurdering
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Periode
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål

fun FlexRespons.medSpørsmålSomSkalStilles(
    gjenbrukbareSpørsmål: List<Spørsmål>
): FlexRespons =
    copy(
        sporsmal = finnSpørsmålSomSkalStilles(
            potensielle = sporsmal,
            forrigeStilte = gjenbrukbareSpørsmål.toSet()
        )
    )

fun FlexRespons.medKjentOppholdstillatelseFra(
    medlemskapVurdering: MedlemskapVurdering
): FlexRespons =
    if (Spørsmål.OPPHOLDSTILATELSE in sporsmal) {
        copy(kjentOppholdstillatelse = medlemskapVurdering.finnKjentOppholdstillatelse())
    } else {
        this
    }

private fun MedlemskapVurdering.finnKjentOppholdstillatelse(): Periode? =
    datagrunnlag
        ?.oppholdstillatelse
        ?.gjeldendeOppholdsstatus
        ?.oppholdstillatelsePaSammeVilkar
        ?.periode
        ?.let { Periode(fom = it.fom, tom = it.tom) }
