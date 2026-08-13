package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.domain.Status

fun List<Medlemskap>.finnGrunnlagForFørstegangssøknaden(påfølgende: Medlemskap): Medlemskap? =
    filter { it.tom < påfølgende.tom && it.medlem != Status.PAFOLGENDE }
        .maxByOrNull { it.tom }

fun List<Medlemskap>.finnMatchendeMedlemskapsperiode(
    medlemskapsstatusRequest: MedlemskapsstatusRequest
): Medlemskap? =
    firstOrNull {
        it.fom.isEqual(medlemskapsstatusRequest.fom) &&
            it.tom.isEqual(medlemskapsstatusRequest.tom)
    }
