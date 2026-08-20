package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstillatelseBrukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.firstMedTagPrefiks

fun hentOppholdstillatelseBrukerspørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>
): MedlemskapOppholdstillatelseBrukerspørsmål? {
    val oppholdstillatelseBrukerspørsmål = finnOppholdstillatelseBrukerspørsmål(spørsmålListe)

    return when (oppholdstillatelseBrukerspørsmål?.tag) {
        "MEDLEMSKAP_OPPHOLDSTILLATELSE_V2" -> mapOppholdstillatelseBrukerSpørsmålV2(oppholdstillatelseBrukerspørsmål)
        "MEDLEMSKAP_OPPHOLDSTILLATELSE" -> mapOppholdstillatelseBrukerSpørsmål(oppholdstillatelseBrukerspørsmål)
        else -> null
    }
}

private fun finnOppholdstillatelseBrukerspørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>
): MedlemskapsBrukerSpørsmål? {
    return spørsmålListe.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_V2")
        ?: spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE" }
}