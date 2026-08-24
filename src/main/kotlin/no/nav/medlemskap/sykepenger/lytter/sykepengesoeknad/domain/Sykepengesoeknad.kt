package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål

data class Sykepengesoeknad(
    val sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag,
    val brukerspørsmål: Brukerspørsmål,
)
