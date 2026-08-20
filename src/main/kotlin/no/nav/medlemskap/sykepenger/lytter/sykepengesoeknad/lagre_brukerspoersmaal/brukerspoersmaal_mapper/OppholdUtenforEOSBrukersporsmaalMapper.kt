package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforEØS
import no.nav.medlemskap.sykepenger.lytter.persistence.OppholdUtenforEØS
import no.nav.medlemskap.sykepenger.lytter.persistence.filterMedTagPrefiks
import no.nav.medlemskap.sykepenger.lytter.persistence.firstMedTagPrefiks
import no.nav.medlemskap.sykepenger.lytter.persistence.førsteSvarVerdi
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.erSvarPåBrukerspørsmålJa

fun hentOppholdUtenforEØSBrukerSpørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>
): MedlemskapOppholdUtenforEØS? {
    val oppholdUtenforEØSbrukerspørsmål =
        spørsmålListe.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS")

    return if (oppholdUtenforEØSbrukerspørsmål != null) {
        mapOppholdUtenforEØSbrukerSpørsmål(oppholdUtenforEØSbrukerspørsmål)
    } else {
        null
    }
}

private fun mapOppholdUtenforEØSbrukerSpørsmål(oppholdutenforEØS: MedlemskapsBrukerSpørsmål): MedlemskapOppholdUtenforEØS {
    val erSvarPåBrukerspørsmålJa = erSvarPåBrukerspørsmålJa(oppholdutenforEØS.svar)

    return MedlemskapOppholdUtenforEØS(
        id = oppholdutenforEØS.id,
        sporsmalstekst = oppholdutenforEØS.sporsmalstekst,
        svar = erSvarPåBrukerspørsmålJa,
        oppholdUtenforEOS = mapOppholdUtenforEØSunderspørsmålVedJaSvar(
            erSvarPåBrukerspørsmålJa,
            oppholdutenforEØS.undersporsmal
        )
    )
}

private fun mapOppholdUtenforEØSunderspørsmålVedJaSvar(
    erSvarPåBrukerspørsmålJa: Boolean,
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): List<OppholdUtenforEØS> {
    if (!erSvarPåBrukerspørsmålJa) {
        return emptyList()
    }

    return mapOppholdUtenforEØSunderspørsmål(underspørsmål)
}

private fun mapOppholdUtenforEØSunderspørsmål(underspørsmål: List<MedlemskapsBrukerSpørsmål>?): List<OppholdUtenforEØS> {
    return underspørsmål.orEmpty()
        .filterMedTagPrefiks("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_GRUPPERING")
        .map { gruppering ->
            mapOppholdUtenforEØSGruppering(gruppering)
        }
}

private fun mapOppholdUtenforEØSGruppering(gruppering: MedlemskapsBrukerSpørsmål): OppholdUtenforEØS {
    val underspørsmål = gruppering.undersporsmal.orEmpty()

    val begrunnelseSpørsmål = underspørsmål
        .firstMedTagPrefiks("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_BEGRUNNELSE")
        ?.undersporsmal

    val begrunnelse = begrunnelseSpørsmål
        ?.find { it.svar?.size == 1 }
        ?.sporsmalstekst

    val land = underspørsmål
        .firstMedTagPrefiks("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_HVOR")!!
        .førsteSvarVerdi()

    val periodeSpørsmål = underspørsmål
        .firstMedTagPrefiks("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_NAAR")!!

    return OppholdUtenforEØS(
        id = gruppering.id,
        land = land,
        grunn = begrunnelse ?: "null",
        perioder = mapBrukerSpørsmålDato(periodeSpørsmål.svar)
    )
}