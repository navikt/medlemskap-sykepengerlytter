package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorgeSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.erSvarPåBrukerspørsmålJa

class BrukersporsmaalMapper(spørsmål: JsonNode) {
    val spørsmålListe: List<MedlemskapsBrukerSpørsmål> =
        objectMapper.convertValue<List<MedlemskapsBrukerSpørsmål>>(spørsmål)
            .filter { it.tag in medlemskapSpørsmålTags }

    val arbeidUtenforNorgeBrukerspørsmål = mapArbeidUtenforNorgeBrukerspørsmål(spørsmålListe)
    val oppholdstilatelseBrukerspørsmål = hentOppholdstillatelseBrukerspørsmål(spørsmålListe)
    val utførtArbeidUtenforNorgeBrukerspørsmål = hentUtførtArbeidUtenforNorgeBrukerSpørsmål(spørsmålListe)
    val oppholdUtenforNorgeSpørsmål = hentOppholdUtenforNorgeBrukerSpørsmål(spørsmålListe)
    val oppholdUtenforEØSbrukerspørsmål = hentOppholdUtenforEØSBrukerSpørsmål(spørsmålListe)

    fun mapArbeidUtenforNorgeBrukerspørsmål(
        spørsmålListe: List<MedlemskapsBrukerSpørsmål>
    ): ArbeidUtenforNorgeSpørsmål {
        val arbeidutland = spørsmålListe.find { it.tag == "ARBEID_UTENFOR_NORGE" }
        var erSvarPåBrukerspørsmålJa: Boolean? = null
        if (arbeidutland?.svar != null)
            erSvarPåBrukerspørsmålJa = erSvarPåBrukerspørsmålJa(arbeidutland.svar)
        return ArbeidUtenforNorgeSpørsmål(erSvarPåBrukerspørsmålJa)
    }

    companion object {
        private val medlemskapSpørsmålTags = setOf(
            "ARBEID_UTENFOR_NORGE",
            "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE",
            "MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE",
            "MEDLEMSKAP_OPPHOLD_UTENFOR_EOS",
            "MEDLEMSKAP_OPPHOLDSTILLATELSE_V2",
            "MEDLEMSKAP_OPPHOLDSTILLATELSE",
        )
    }
}