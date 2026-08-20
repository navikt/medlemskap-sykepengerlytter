package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.OppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.filterMedTagPrefiks
import no.nav.medlemskap.sykepenger.lytter.persistence.firstMedTagPrefiks
import no.nav.medlemskap.sykepenger.lytter.persistence.førsteSvarVerdi
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.erSvarPåBrukerspørsmålJa

fun hentOppholdUtenforNorgeBrukerSpørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>
): MedlemskapOppholdUtenforNorge? {
    val oppholdUtenforNorgebrukerspørsmål =
        spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE" }

    return if (oppholdUtenforNorgebrukerspørsmål != null) {
        mapOppholdUtenforNorgeBrukerSpørsmål(oppholdUtenforNorgebrukerspørsmål)
    } else {
        null
    }
}

private fun mapOppholdUtenforNorgeBrukerSpørsmål(
    oppholdUtenforNorge: MedlemskapsBrukerSpørsmål
): MedlemskapOppholdUtenforNorge {
    val erSvarPåBrukerspørsmålJa = erSvarPåBrukerspørsmålJa(oppholdUtenforNorge.svar)
    return MedlemskapOppholdUtenforNorge(
        id = oppholdUtenforNorge.id,
        sporsmalstekst = oppholdUtenforNorge.sporsmalstekst,
        svar = erSvarPåBrukerspørsmålJa,
        oppholdUtenforNorge = mapOppholdUtenforNorgeUnderspørsmålVedJaSvar(
            erSvarPåBrukerspørsmålJa,
            oppholdUtenforNorge.undersporsmal
        )
    )
}

private fun mapOppholdUtenforNorgeUnderspørsmålVedJaSvar(
    svar: Boolean,
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): List<OppholdUtenforNorge> {
    if (!svar) {
        return emptyList()
    }

    return mapOppholdUtenforNorgeUnderspørsmål(underspørsmål)
}

private fun mapOppholdUtenforNorgeUnderspørsmål(
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): List<OppholdUtenforNorge> {
    return underspørsmål.orEmpty()
        .filterMedTagPrefiks("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_GRUPPERING")
        .map { gruppering ->
            mapOppholdUtenforNorgeGruppering(gruppering)
        }
}

private fun mapOppholdUtenforNorgeGruppering(
    gruppering: MedlemskapsBrukerSpørsmål
): OppholdUtenforNorge {
    val underspørsmål = gruppering.undersporsmal.orEmpty()

    val begrunnelseUnderspørsmål = underspørsmål
        .firstMedTagPrefiks("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_BEGRUNNELSE")
        ?.undersporsmal

    val begrunnelse = begrunnelseUnderspørsmål
        ?.find { it.svar?.size == 1 }
        ?.sporsmalstekst

    val land = underspørsmål
        .firstMedTagPrefiks("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_HVOR")!!
        .førsteSvarVerdi()

    val periodeSpørsmål = underspørsmål
        .firstMedTagPrefiks("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_NAAR")!!

    return OppholdUtenforNorge(
        id = gruppering.id,
        land = land,
        grunn = begrunnelse ?: "null",
        perioder = mapBrukerSpørsmålDato(periodeSpørsmål.svar)
    )
}
