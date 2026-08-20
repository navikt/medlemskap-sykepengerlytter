package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål

class BrukersporsmaalMapper(spørsmål: JsonNode) {
    val spørsmålListe: List<MedlemskapsBrukerSpørsmål> =
        objectMapper.convertValue<List<MedlemskapsBrukerSpørsmål>>(spørsmål)
            .filter { it.tag in medlemskapSpørsmålTags }

    val arbeidUtenforNorgeBrukerspørsmål = mapArbeidUtenforNorgeBrukerspørsmål(spørsmålListe)
    val oppholdstilatelseBrukerspørsmål = hentOppholdstillatelseBrukerspørsmål(spørsmålListe)
    val utførtArbeidUtenforNorgeBrukerspørsmål = hentUtførtArbeidUtenforNorgeBrukerSpørsmål(spørsmålListe)
    val oppholdUtenforNorgeSpørsmål = hentOppholdUtenforNorgeBrukerSpørsmål(spørsmålListe)
    val oppholdUtenforEØSbrukerspørsmål = hentOppholdUtenforEØSBrukerSpørsmål(spørsmålListe)

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