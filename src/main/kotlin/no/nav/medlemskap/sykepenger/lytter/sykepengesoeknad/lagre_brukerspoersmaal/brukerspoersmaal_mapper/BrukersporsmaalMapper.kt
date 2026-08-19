package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorgeSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapSvar

class BrukersporsmaalMapper(spørsmål: JsonNode) {
    val spørsmålListe: List<MedlemskapsBrukerSpørsmål> =
        objectMapper.convertValue<List<MedlemskapsBrukerSpørsmål>>(spørsmål)
            .filter { it.tag in medlemskapSpørsmålTags }

    val oppholdstilatelseBrukerspørsmål = hentOppholdstillatelseBrukerspørsmål(
        spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_V2" },
        spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE" },
    )
    val arbeidUtenforNorgeBrukerspørsmål: ArbeidUtenforNorgeSpørsmål =
        mapArbeidUtenforNorgeBrukerspørsmål(spørsmålListe.find { it.tag == "ARBEID_UTENFOR_NORGE" })
    val utførtArbeidUtenforNorgeBrukerspørsmål =
        hentUtførtArbeidUtenforNorgeBrukerSpørsmål(
            spørsmålListe.find { it.tag == "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE" }
        )
    val oppholdUtenforNorgeSpørsmål =
        getOppholdUtenforNorgeBrukerSporsmaal(spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE" })
    val oppholdUtenforEØSbrukerspørsmål =
        getOppholdUtenforEOSBrukerSporsmaal(spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLD_UTENFOR_EOS" })

    fun mapArbeidUtenforNorgeBrukerspørsmål(arbeidutland: MedlemskapsBrukerSpørsmål?): ArbeidUtenforNorgeSpørsmål {
        var svar: Boolean? = null
        if (arbeidutland?.svar != null)
            svar = mapSvar(arbeidutland.svar)
        return ArbeidUtenforNorgeSpørsmål(svar)
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