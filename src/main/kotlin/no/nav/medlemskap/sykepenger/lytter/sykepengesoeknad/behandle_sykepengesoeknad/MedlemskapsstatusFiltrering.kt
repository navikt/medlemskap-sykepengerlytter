package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap

fun Medlemskap.erpåfølgende(medlemskap: Medlemskap): Boolean =
    fom.isEqual(medlemskap.tom.plusDays(1)) || erIMidtenAv(medlemskap)

fun Medlemskap.erFunkskjoneltLik(medlemskap: Medlemskap): Boolean =
    fom.isEqual(medlemskap.fom) && tom.isEqual(medlemskap.tom)

private fun Medlemskap.erIMidtenAv(medlemskap: Medlemskap): Boolean =
    fom.isAfter(medlemskap.fom) && fom.isBefore(medlemskap.tom)
