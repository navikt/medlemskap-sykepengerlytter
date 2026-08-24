package no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain

import com.fasterxml.jackson.databind.JsonNode

data class Medlemskapsvurdering(
    val json: JsonNode
)