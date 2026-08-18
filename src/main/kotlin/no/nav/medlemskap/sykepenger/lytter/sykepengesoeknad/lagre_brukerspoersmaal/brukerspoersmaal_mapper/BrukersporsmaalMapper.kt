package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapSvar

class BrukersporsmaalMapper(sporsmal: JsonNode) {
    val spørsmålListe: List<FlexMedlemskapsBrukerSporsmaal> =
        objectMapper.convertValue<List<FlexMedlemskapsBrukerSporsmaal>>(sporsmal)
            .filter { it.tag in medlemskapSpoersmaalTags }

    val oppholdstilatelseBrukersporsmaal = getOppholdstillatelseBrukersporsmaal(
        spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_V2" },
        spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE" },
    )
    val brukersporsmaalArbeidUtlandOldModel: FlexBrukerSporsmaal =
        FlexBrukerSporsmaalmapArbeidUtlandOldModel(spørsmålListe.find { it.tag == "ARBEID_UTENFOR_NORGE" })
    val arbeidUtlandBrukerSporsmaal =
        getutfoertArbeidUtenforNorgeBrukerSporsmaal(
            spørsmålListe.find { it.tag == "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE" }
        )
    val oppholdUtenforNorge =
        getOppholdUtenforNorgeBrukerSporsmaal(spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE" })
    val oppholdUtenforEOS =
        getOppholdUtenforEOSBrukerSporsmaal(spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLD_UTENFOR_EOS" })

    fun FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidutland: FlexMedlemskapsBrukerSporsmaal?): FlexBrukerSporsmaal {
        var svar: Boolean? = null
        if (arbeidutland?.svar != null)
            svar = mapSvar(arbeidutland.svar)
        return FlexBrukerSporsmaal(svar)
    }

    companion object {
        private val medlemskapSpoersmaalTags = setOf(
            "ARBEID_UTENFOR_NORGE",
            "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE",
            "MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE",
            "MEDLEMSKAP_OPPHOLD_UTENFOR_EOS",
            "MEDLEMSKAP_OPPHOLDSTILLATELSE_V2",
            "MEDLEMSKAP_OPPHOLDSTILLATELSE",
        )
    }
}