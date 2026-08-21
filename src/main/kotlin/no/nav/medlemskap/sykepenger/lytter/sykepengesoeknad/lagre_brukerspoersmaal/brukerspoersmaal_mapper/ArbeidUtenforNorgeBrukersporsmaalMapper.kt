package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorgeSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.førsteSvarVerdi

fun mapArbeidUtenforNorgeBrukerspørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>
): ArbeidUtenforNorgeSpørsmål {
    val arbeidUtenforNorgeSvarVerdi = spørsmålListe
        .find { it.tag == "ARBEID_UTENFOR_NORGE" }
        ?.takeIf { !it.svar.isNullOrEmpty() }?.førsteSvarVerdi()

    val erSvarPåArbeidUtenforNorgeJa = when (arbeidUtenforNorgeSvarVerdi) {
        "JA" -> true
        "NEI" -> false
        else -> null
    }
    return ArbeidUtenforNorgeSpørsmål(erSvarPåArbeidUtenforNorgeJa)
}
