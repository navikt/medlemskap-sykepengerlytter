package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstillatelseBrukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import no.nav.medlemskap.sykepenger.lytter.persistence.firstMedTagPrefiks
import no.nav.medlemskap.sykepenger.lytter.persistence.førsteSvarVerdi
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.erSvarPåBrukerspørsmålJa
import java.time.LocalDate

internal fun mapOppholdstillatelseBrukerSpørsmål(
    oppholdstillatelseBrukerspørsmål: MedlemskapsBrukerSpørsmål
): MedlemskapOppholdstillatelseBrukerspørsmål {
    val vedtaksdato = hentVedtaksdatoFraUnderspørsmål(oppholdstillatelseBrukerspørsmål.undersporsmal)
    val vedtakstype =
        permanentEllerMidlertidigVedtaksTypeFraUnderspørsmål(oppholdstillatelseBrukerspørsmål.undersporsmal)

    return lagOppholdstillatelseBrukerspørsmål(
        oppholdstillatelseBrukerspørsmål = oppholdstillatelseBrukerspørsmål,
        svar = erSvarPåBrukerspørsmålJa(oppholdstillatelseBrukerspørsmål.svar),
        vedtaksdato = LocalDate.parse(vedtaksdato),
        vedtaksTypePermanent = vedtakstype.erPermanentVedtaksType,
        perioder = vedtakstype.periode
    )
}

private fun permanentEllerMidlertidigVedtaksTypeFraUnderspørsmål(
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): VedtaksType {
    val oppholdstillatelseSpørsmålGruppering =
        underspørsmål?.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_GRUPPE")

    return if (oppholdstillatelseSpørsmålGruppering == null) {
        mapVedtakstypeUtenGruppering(underspørsmål)
    } else {
        mapVedtakstypeMedGruppering(oppholdstillatelseSpørsmålGruppering.undersporsmal)
    }
}

private fun mapVedtakstypeUtenGruppering(
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): VedtaksType {
    val oppholdstillatelsePermanentBrukerspørsmål =
        underspørsmål?.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT")
            ?: return VedtaksType(erPermanentVedtaksType = false, periode = emptyList())

    val erVedtakstypePermanent = erSvarPåBrukerspørsmålJa(oppholdstillatelsePermanentBrukerspørsmål.svar)
    val permanentPeriodeSpørsmål = oppholdstillatelsePermanentBrukerspørsmål.undersporsmal
        ?.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE")

    return VedtaksType(
        erPermanentVedtaksType = erVedtakstypePermanent,
        periode = mapBrukerSpørsmålDato(permanentPeriodeSpørsmål?.svar)
    )
}

private fun mapVedtakstypeMedGruppering(
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): VedtaksType {
    val oppholdstillatelseMidlertidigSpørsmål =
        underspørsmål?.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_MIDLERTIDIG")

    val oppholdstillatelsePermanentBrukerspørsmål =
        underspørsmål?.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT")

    return mapMidlertidigVedtak(oppholdstillatelseMidlertidigSpørsmål)
        ?: mapPermanentVedtak(oppholdstillatelsePermanentBrukerspørsmål)
        ?: VedtaksType(erPermanentVedtaksType = false, periode = emptyList())
}

private fun mapPermanentVedtak(
    oppholdstillatelsePermanentBrukerspørsmål: MedlemskapsBrukerSpørsmål?
): VedtaksType? {
    if (oppholdstillatelsePermanentBrukerspørsmål?.svar?.isNotEmpty() != true) {
        return null
    }

    val fom = LocalDate.parse(
        oppholdstillatelsePermanentBrukerspørsmål.undersporsmal
            ?.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT_DATO")
            ?.førsteSvarVerdi()
    )

    return VedtaksType(
        erPermanentVedtaksType = true,
        periode = listOf(Periode(fom, LocalDate.MAX))
    )
}

private fun mapMidlertidigVedtak(
    oppholdstillatelseMidlertidigSpørsmål: MedlemskapsBrukerSpørsmål?
): VedtaksType? {
    if (oppholdstillatelseMidlertidigSpørsmål?.svar?.isNotEmpty() != true) {
        return null
    }

    val midlertidigPeriodeSpørsmål = oppholdstillatelseMidlertidigSpørsmål.undersporsmal
        ?.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_MIDLERTIDIG_PERIODE")

    return VedtaksType(
        erPermanentVedtaksType = false,
        periode = mapBrukerSpørsmålDato(midlertidigPeriodeSpørsmål?.svar)
    )
}
