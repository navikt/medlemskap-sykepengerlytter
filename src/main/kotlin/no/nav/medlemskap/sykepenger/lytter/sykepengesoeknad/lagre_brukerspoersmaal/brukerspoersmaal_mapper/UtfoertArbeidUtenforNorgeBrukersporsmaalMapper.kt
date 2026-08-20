package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapUtførtArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.filterMedTagPrefiks
import no.nav.medlemskap.sykepenger.lytter.persistence.firstMedTagPrefiks
import no.nav.medlemskap.sykepenger.lytter.persistence.førsteSvarVerdi
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.erSvarPåBrukerspørsmålJa

fun hentUtførtArbeidUtenforNorgeBrukerSpørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>
): MedlemskapUtførtArbeidUtenforNorge? {
    val utførtArbeidUtenforNorgeSpørsmål =
        spørsmålListe.firstMedTagPrefiks("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE")

    return if (utførtArbeidUtenforNorgeSpørsmål != null) {
        mapUtførtArbeidUtenforNorgeBrukerSpørsmål(utførtArbeidUtenforNorgeSpørsmål)
    } else {
        null
    }
}

private fun mapUtførtArbeidUtenforNorgeBrukerSpørsmål(
    utførtArbeidUtenforNorgeSpørsmål: MedlemskapsBrukerSpørsmål
): MedlemskapUtførtArbeidUtenforNorge {
    val erSvarPåBrukerspørsmålJa = erSvarPåBrukerspørsmålJa(utførtArbeidUtenforNorgeSpørsmål.svar)

    return MedlemskapUtførtArbeidUtenforNorge(
        id = utførtArbeidUtenforNorgeSpørsmål.id,
        spørsmålstekst = utførtArbeidUtenforNorgeSpørsmål.sporsmalstekst,
        svar = erSvarPåBrukerspørsmålJa,
        arbeidUtenforNorge = mapUtførtArbeidUtenforNorgeUnderspørsmålVedJaSvar(
            erSvarPåBrukerspørsmålJa,
            utførtArbeidUtenforNorgeSpørsmål.undersporsmal
        )
    )
}

private fun mapUtførtArbeidUtenforNorgeUnderspørsmålVedJaSvar(
    erSvarPåBrukerspørsmålJa: Boolean,
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): List<ArbeidUtenforNorge> {
    if (!erSvarPåBrukerspørsmålJa) {
        return emptyList()
    }

    return mapUtførtArbeidUtenforNorgeUnderspørsmål(underspørsmål)
}

private fun mapUtførtArbeidUtenforNorgeUnderspørsmål(underspørsmål: List<MedlemskapsBrukerSpørsmål>?): List<ArbeidUtenforNorge> {
    return underspørsmål.orEmpty()
        .filterMedTagPrefiks("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_GRUPPERING")
        .map { gruppering ->
            mapUtførtArbeidUtenforNorgeGruppering(gruppering)
        }
}

private fun mapUtførtArbeidUtenforNorgeGruppering(gruppering: MedlemskapsBrukerSpørsmål): ArbeidUtenforNorge {
    val underspørsmål = gruppering.undersporsmal.orEmpty()

    val arbeidsgiver = underspørsmål
        .firstMedTagPrefiks("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_ARBEIDSGIVER")
        ?.førsteSvarVerdi()

    val land = underspørsmål
        .firstMedTagPrefiks("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_HVOR")!!
        .førsteSvarVerdi()

    val periodeSpørsmål = underspørsmål
        .firstMedTagPrefiks("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_NAAR")!!

    return ArbeidUtenforNorge(
        id = gruppering.id,
        arbeidsgiver = arbeidsgiver ?: "null",
        land = land,
        perioder = mapBrukerSpørsmålDato(periodeSpørsmål.svar)
    )
}
