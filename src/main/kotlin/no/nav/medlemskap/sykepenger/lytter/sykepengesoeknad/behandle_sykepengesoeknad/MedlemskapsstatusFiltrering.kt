package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.Vurderingsstatus

fun Vurderingsstatus.erpåfølgende(vurderingsstatus: Vurderingsstatus): Boolean =
    fom.isEqual(vurderingsstatus.tom.plusDays(1)) || erIMidtenAv(vurderingsstatus)

fun Vurderingsstatus.erFunkskjoneltLik(vurderingsstatus: Vurderingsstatus): Boolean =
    fom.isEqual(vurderingsstatus.fom) && tom.isEqual(vurderingsstatus.tom)

private fun Vurderingsstatus.erIMidtenAv(vurderingsstatus: Vurderingsstatus): Boolean =
    fom.isAfter(vurderingsstatus.fom) && fom.isBefore(vurderingsstatus.tom)
