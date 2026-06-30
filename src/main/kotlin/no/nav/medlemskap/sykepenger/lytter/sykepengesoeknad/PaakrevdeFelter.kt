package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.LovmeSoknadDTO

fun harPåkrevdeFelter(sykepengesøknad: LovmeSoknadDTO): Boolean =
    sykepengesøknad.fnr.isNotBlank() && sykepengesøknad.id.isNotBlank()
