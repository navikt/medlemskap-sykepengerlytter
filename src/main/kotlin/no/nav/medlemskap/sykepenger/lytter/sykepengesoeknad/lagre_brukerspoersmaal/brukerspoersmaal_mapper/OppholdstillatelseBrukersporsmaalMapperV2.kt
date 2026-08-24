package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstillatelseBrukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import no.nav.medlemskap.sykepenger.lytter.persistence.firstMedTagPrefiks
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.erSvarPåBrukerspørsmålJa
import java.time.LocalDate

internal fun mapOppholdstillatelseBrukerSpørsmålV2(
    oppholdstillatelseBrukerspørsmål: MedlemskapsBrukerSpørsmål
): MedlemskapOppholdstillatelseBrukerspørsmål {
    val erSvarPåBrukerspørsmålJa = erSvarPåBrukerspørsmålJa(oppholdstillatelseBrukerspørsmål.svar)

    mapOppholdstillatelseBrukerspørsmålVedNeiSvar(oppholdstillatelseBrukerspørsmål, erSvarPåBrukerspørsmålJa)
        ?.let { return it }

    val vedtakstype = permanentEllerMidlertidigVedtakstypeFraUnderspørsmålV2(oppholdstillatelseBrukerspørsmål)
    val vedtaksdato = hentVedtaksdatoFraUnderspørsmål(oppholdstillatelseBrukerspørsmål.undersporsmal)

    return lagOppholdstillatelseBrukerspørsmål(
        oppholdstillatelseBrukerspørsmål = oppholdstillatelseBrukerspørsmål,
        svar = erSvarPåBrukerspørsmålJa,
        vedtaksdato = LocalDate.parse(vedtaksdato),
        vedtaksTypePermanent = vedtakstype.erPermanentVedtaksType,
        perioder = vedtakstype.periode
    )
}

private fun permanentEllerMidlertidigVedtakstypeFraUnderspørsmålV2(
    oppholdstillatelseBrukerspørsmål: MedlemskapsBrukerSpørsmål
): VedtaksType {
    val oppholdstillatelsePeriodeSpørsmål =
        oppholdstillatelseBrukerspørsmål
            .undersporsmal?.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE")

    var vedtaksperiode: List<Periode> = emptyList()
    var vedtakstype = false

    if (oppholdstillatelsePeriodeSpørsmål != null && oppholdstillatelsePeriodeSpørsmål.svar?.isNotEmpty() == true) {
        vedtaksperiode = mapBrukerSpørsmålDato(oppholdstillatelsePeriodeSpørsmål.svar)
        vedtakstype = false
    }

    return VedtaksType(
        erPermanentVedtaksType = vedtakstype,
        periode = vedtaksperiode
    )
}

private fun mapOppholdstillatelseBrukerspørsmålVedNeiSvar(
    oppholdstillatelseBrukerspørsmål: MedlemskapsBrukerSpørsmål,
    erSvarPåBrukerspørsmålJa: Boolean
): MedlemskapOppholdstillatelseBrukerspørsmål? {
    if (erSvarPåBrukerspørsmålJa) {
        return null
    }

    return lagOppholdstillatelseBrukerspørsmål(
        oppholdstillatelseBrukerspørsmål = oppholdstillatelseBrukerspørsmål,
        svar = erSvarPåBrukerspørsmålJa,
        vedtaksdato = LocalDate.now(),
        vedtaksTypePermanent = false,
        perioder = emptyList()
    )
}
