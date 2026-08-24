package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorgeSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål

fun mapArbeidUtenforNorgeBrukerspørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>
): ArbeidUtenforNorgeSpørsmål {
    val arbeidUtenforNorgeSvarVerdi = spørsmålListe
        .find { it.tag == "ARBEID_UTENFOR_NORGE" }
        ?.svar?.firstOrNull()?.verdi
    val erSvarPåArbeidUtenforNorgeJa = when (arbeidUtenforNorgeSvarVerdi) {
        "JA" -> true
        "NEI" -> false
        else -> null
    }
    return ArbeidUtenforNorgeSpørsmål(erSvarPåArbeidUtenforNorgeJa)
}
