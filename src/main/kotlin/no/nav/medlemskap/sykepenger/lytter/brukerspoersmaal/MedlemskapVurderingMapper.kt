package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapVurdering

class MedlemskapVurderingMapper {
    fun map(medlemskapOppslagResponse: String): MedlemskapVurdering =
        objectMapper.readValue(medlemskapOppslagResponse)
}
