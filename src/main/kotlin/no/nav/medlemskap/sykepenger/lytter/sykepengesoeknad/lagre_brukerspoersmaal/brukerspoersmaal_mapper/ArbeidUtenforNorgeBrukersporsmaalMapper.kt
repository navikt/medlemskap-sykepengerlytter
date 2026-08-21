package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorgeSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.erSvarPåBrukerspørsmålJa

fun mapArbeidUtenforNorgeBrukerspørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>
): ArbeidUtenforNorgeSpørsmål {
    val arbeidutland = spørsmålListe.find { it.tag == "ARBEID_UTENFOR_NORGE" }
    var erSvarPåArbeidUtenforNorgeJa: Boolean? = null
    if (arbeidutland?.svar != null)
        erSvarPåArbeidUtenforNorgeJa = erSvarPåBrukerspørsmålJa(arbeidutland.svar)
    return ArbeidUtenforNorgeSpørsmål(erSvarPåArbeidUtenforNorgeJa)
}
