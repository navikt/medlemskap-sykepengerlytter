package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal

data class Sykepengesoeknad(
    val sykepengesoeknadGrunnlag: SykepengesoeknadGrunnlag,
    val brukersporsmaal: Brukersporsmaal,
)
