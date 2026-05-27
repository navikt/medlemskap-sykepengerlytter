package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.domain.Brukerinput

data class MedlemskapVurdering(
    val vurderingsId: String,
    val kanal: String,
    val fnr: String,
    val ytelse: String,
    val brukerinput: Brukerinput,
    val svar: String,
    val status: String,
)

class MedlemskapVurdertMapper {

    fun tilMedlemskapVurdering(value: String): MedlemskapVurdering {
        val root = objectMapper.readTree(value)
        val datagrunnlag = root.requiredField("datagrunnlag")

        return MedlemskapVurdering(
            vurderingsId = root.requiredText("vurderingsID"),
            kanal = root.requiredText("kanal"),
            fnr = datagrunnlag.requiredText("fnr"),
            ytelse = datagrunnlag.requiredText("ytelse"),
            brukerinput = objectMapper.treeToValue(datagrunnlag.requiredField("brukerinput")),
            svar = root.requiredField("resultat").requiredText("svar"),
            status = root.statusFraKonklusjon() ?: "",
        )
    }

    private fun JsonNode.requiredText(fieldName: String): String =
        requiredField(fieldName).takeIf { it.isTextual }
            ?.asText()
            ?: throw IllegalArgumentException("Mangler tekstfelt '$fieldName' i medlemskap-vurdert-melding")

    private fun JsonNode.requiredField(fieldName: String): JsonNode =
        get(fieldName).takeIf { it != null && !it.isNull }
            ?: throw IllegalArgumentException("Mangler felt '$fieldName' i medlemskap-vurdert-melding")

    private fun JsonNode.requiredElement(index: Int): JsonNode =
        takeIf { it.isArray && it.size() > index }
            ?.get(index)
            ?: throw IllegalArgumentException("Mangler element '$index' i medlemskap-vurdert-melding")

    private fun JsonNode.statusFraKonklusjon(): String? {
        val konklusjon = get("konklusjon").takeIf { it != null && !it.isNull } ?: return null
        return konklusjon.requiredElement(0).requiredText("status")
    }
}
