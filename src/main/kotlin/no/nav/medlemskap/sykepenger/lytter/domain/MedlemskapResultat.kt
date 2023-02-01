package no.nav.medlemskap.sykepenger.lytter.domain

import com.fasterxml.jackson.databind.JsonNode

data class MedlemskapResultat(val fnr: String, val svar: String, val årsak: String?, val årsaker: List<JsonNode>)

fun JsonNode.lagMedlemskapsResultat(): MedlemskapResultat {
    val årsaker = this.get("resultat").get("årsaker")

    return MedlemskapResultat(
        fnr = this.get("datagrunnlag").get("fnr").asText(),
        svar = this.get("resultat").get("svar").asText(),
        årsak = årsaker.firstOrNull()?.get("regelId")?.asText(),
        årsaker = årsaker.map { it.get("regelId") }
    )
}

