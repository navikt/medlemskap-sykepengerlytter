package no.nav.medlemskap.sykepenger.lytter.domain

import com.fasterxml.jackson.databind.JsonNode

data class MedlemskapOppslagVurdering(
    val vurderingsID: String? = null,
    val kanal: String? = null,
    val datagrunnlag: JsonNode,
    val resultat: JsonNode
)