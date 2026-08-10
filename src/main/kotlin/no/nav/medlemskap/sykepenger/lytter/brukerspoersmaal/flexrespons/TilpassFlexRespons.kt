package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.flexrespons

import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapVurdering
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Periode
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar

fun Set<Spørsmål>.tilFlexRespons(
    medlemskapVurdering: MedlemskapVurdering
): FlexRespons = FlexRespons(
    svar = medlemskapVurdering.tilSvar(),
    sporsmal = this,
    kjentOppholdstillatelse = if (Spørsmål.OPPHOLDSTILATELSE in this) {
        medlemskapVurdering.finnKjentOppholdstillatelse()
    } else {
        null
    }
)

private fun MedlemskapVurdering.tilSvar(): Svar =
    when (resultat.svar) {
        "JA" -> Svar.JA
        "NEI" -> Svar.NEI
        "UAVKLART" -> Svar.UAVKLART
        else -> throw IllegalStateException("Ukjent svar fra regelmotor: ${resultat.svar}")
    }

private fun MedlemskapVurdering.finnKjentOppholdstillatelse(): Periode? =
    datagrunnlag
        ?.oppholdstillatelse
        ?.gjeldendeOppholdsstatus
        ?.oppholdstillatelsePaSammeVilkar
        ?.periode
        ?.let { Periode(fom = it.fom, tom = it.tom) }
