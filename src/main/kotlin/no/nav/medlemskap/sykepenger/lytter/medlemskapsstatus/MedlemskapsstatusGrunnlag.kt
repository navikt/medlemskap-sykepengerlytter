package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import no.nav.medlemskap.sykepenger.lytter.domain.Vurderingsstatus
import no.nav.medlemskap.sykepenger.lytter.domain.Status

fun List<Vurderingsstatus>.finnGrunnlagForFørstegangssøknaden(påfølgende: Vurderingsstatus): Vurderingsstatus? =
    filter { it.tom < påfølgende.tom && it.status != Status.PAFOLGENDE }
        .maxByOrNull { it.tom }

fun List<Vurderingsstatus>.finnMatchendeMedlemskapsperiode(
    medlemskapsstatusRequest: MedlemskapsstatusRequest
): Vurderingsstatus? =
    firstOrNull {
        it.fom.isEqual(medlemskapsstatusRequest.fom) &&
            it.tom.isEqual(medlemskapsstatusRequest.tom)
    }
