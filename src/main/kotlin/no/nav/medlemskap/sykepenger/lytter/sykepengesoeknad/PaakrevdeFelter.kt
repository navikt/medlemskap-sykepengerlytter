package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag

fun harPåkrevdeFelter(sykepengesøknad: SykepengesoeknadGrunnlag): Boolean =
    sykepengesøknad.fnr.isNotBlank() && sykepengesøknad.id.isNotBlank()
