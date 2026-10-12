package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.Klassifisering

fun Klassifisering.erpåfølgende(klassifisering: Klassifisering): Boolean =
    fom.isEqual(klassifisering.tom.plusDays(1)) || erIMidtenAv(klassifisering)

fun Klassifisering.erFunkskjoneltLik(klassifisering: Klassifisering): Boolean =
    fom.isEqual(klassifisering.fom) && tom.isEqual(klassifisering.tom)

private fun Klassifisering.erIMidtenAv(klassifisering: Klassifisering): Boolean =
    fom.isAfter(klassifisering.fom) && fom.isBefore(klassifisering.tom)
