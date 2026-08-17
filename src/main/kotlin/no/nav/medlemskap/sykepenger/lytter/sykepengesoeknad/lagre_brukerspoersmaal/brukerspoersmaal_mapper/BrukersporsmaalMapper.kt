package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapSvar

class BrukersporsmaalMapper(sporsmal: JsonNode) {
    val spoersmaalListe: List<FlexMedlemskapsBrukerSporsmaal> = objectMapper.convertValue(sporsmal)

    val arbeidUtland = spoersmaalListe.find { it.tag == "ARBEID_UTENFOR_NORGE" }
    val utfoertArbeidUtenforNorge = spoersmaalListe.find { it.tag == "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE" }
    val oppholdUtenforNorgeSpoersmaal = spoersmaalListe.find { it.tag == "MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE" }
    val oppholdUtenforEOSSpoersmaal = spoersmaalListe.find { it.tag == "MEDLEMSKAP_OPPHOLD_UTENFOR_EOS" }

    val oppholdstilatelseBrukersporsmaal = getOppholdstillatelseBrukersporsmaal(spoersmaalListe)
    val brukersporsmaalArbeidUtlandOldModel: FlexBrukerSporsmaal = FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidUtland)
    val arbeidUtlandBrukerSporsmaal = getutfoertArbeidUtenforNorgeBrukerSporsmaal(utfoertArbeidUtenforNorge)
    val oppholdUtenforNorge = getOppholdUtenforNorgeBrukerSporsmaal(oppholdUtenforNorgeSpoersmaal)
    val oppholdUtenforEOS = getOppholdUtenforEOSBrukerSporsmaal(oppholdUtenforEOSSpoersmaal)

    fun FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidutland: FlexMedlemskapsBrukerSporsmaal?): FlexBrukerSporsmaal {
        var svar: Boolean? = null
        if (arbeidutland?.svar != null)
            svar = mapSvar(arbeidutland.svar)
        return FlexBrukerSporsmaal(svar)
    }
}