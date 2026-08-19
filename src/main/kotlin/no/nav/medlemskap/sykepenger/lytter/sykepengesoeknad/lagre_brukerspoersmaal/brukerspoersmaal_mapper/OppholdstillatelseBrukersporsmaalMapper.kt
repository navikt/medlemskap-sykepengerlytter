package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstillatelseBrukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapSvar
import java.time.LocalDate

fun hentOppholdstillatelseBrukerspørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>,
): MedlemskapOppholdstillatelseBrukerspørsmål? {
    val oppholdstillatelseBrukerspørsmålV2 =
        spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_V2" }
    val oppholdstillatelseBrukerspørsmål =
        spørsmålListe.find { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE" }

    return if (oppholdstillatelseBrukerspørsmålV2 != null) {
        mapOppholdstilateleBrukerSpørsmål_v2(oppholdstillatelseBrukerspørsmålV2)
    } else if (oppholdstillatelseBrukerspørsmål != null) {
        mapOppholdstillatelseBrukerSpørsmål(oppholdstillatelseBrukerspørsmål)
    } else {
        null
    }
}

private fun hentVedtaksdatoFraUnderspørsmål(underspørsmål: List<MedlemskapsBrukerSpørsmål>?): String {
    return underspørsmål?.first { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_VEDTAKSDATO" }?.svar?.first()?.verdi
        ?: "null"
}

private fun permanentEllerMidlertidigVedtaksTypeFraUnderspørsmål(
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): VedtaksType {
    val oppholdstillatelseSpørsmålGruppering =
        underspørsmål?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_GRUPPE" }

    if (oppholdstillatelseSpørsmålGruppering == null) {
        val oppholdstillatelsePermanentBrukerspørsmålUtenGruppering =
            underspørsmål?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT" }
                ?: return VedtaksType(erPermanentVedtaksType = false, periode = emptyList())

        val erVedtakstypePermanent = mapSvar(oppholdstillatelsePermanentBrukerspørsmålUtenGruppering.svar)
        val permanentPeriodeSpørsmål = oppholdstillatelsePermanentBrukerspørsmålUtenGruppering.undersporsmal
            ?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE" }

        return VedtaksType(
            erPermanentVedtaksType = erVedtakstypePermanent,
            periode = mapBrukerSpørsmålDato(permanentPeriodeSpørsmål?.svar),
        )
    }

    val oppholdstillatelseMidlertidigSpørsmål =
        oppholdstillatelseSpørsmålGruppering
            .undersporsmal?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_MIDLERTIDIG" }

    val oppholdstillatelsePermanentBrukerspørsmål =
        oppholdstillatelseSpørsmålGruppering
            .undersporsmal?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT" }

    var erVedtakstypePermanent = false
    var periode: List<Periode> = emptyList()

    if (oppholdstillatelsePermanentBrukerspørsmål != null && oppholdstillatelsePermanentBrukerspørsmål.svar?.isNotEmpty() == true) {
        erVedtakstypePermanent = true
        val fom = LocalDate.parse(
            oppholdstillatelsePermanentBrukerspørsmål.undersporsmal
                ?.first { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT_DATO" }?.svar?.first()?.verdi
        )
        periode = listOf(Periode(fom, LocalDate.MAX))
    }

    if (oppholdstillatelseMidlertidigSpørsmål != null && oppholdstillatelseMidlertidigSpørsmål.svar?.isNotEmpty() == true) {
        erVedtakstypePermanent = false
        val midlertidigPeriodeSpørsmål = oppholdstillatelseMidlertidigSpørsmål.undersporsmal
            ?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_MIDLERTIDIG_PERIODE" }
        periode = mapBrukerSpørsmålDato(midlertidigPeriodeSpørsmål?.svar)
    }

    return VedtaksType(
        erPermanentVedtaksType = erVedtakstypePermanent,
        periode = periode,
    )
}

private fun mapOppholdstillatelseBrukerSpørsmål(
    oppholdstillatelseBrukerspørsmål: MedlemskapsBrukerSpørsmål
): MedlemskapOppholdstillatelseBrukerspørsmål {
    val vedtaksdato = hentVedtaksdatoFraUnderspørsmål(oppholdstillatelseBrukerspørsmål.undersporsmal)
    val vedtakstype =
        permanentEllerMidlertidigVedtaksTypeFraUnderspørsmål(oppholdstillatelseBrukerspørsmål.undersporsmal)
    return MedlemskapOppholdstillatelseBrukerspørsmål(
        id = oppholdstillatelseBrukerspørsmål.id,
        spørsmalstekst = oppholdstillatelseBrukerspørsmål.sporsmalstekst,
        svar = mapSvar(oppholdstillatelseBrukerspørsmål.svar),
        vedtaksdato = LocalDate.parse(vedtaksdato),
        vedtaksTypePermanent = vedtakstype.erPermanentVedtaksType,
        perioder = vedtakstype.periode,
    )
}

private fun permanentEllerMidlertidigVedtakstypeFraUnderspørsmålV2(
    oppholdstillatelseBrukerspørsmål: MedlemskapsBrukerSpørsmål
): VedtaksType {
    val oppholdstillatelsePeriodeSpørsmål =
        oppholdstillatelseBrukerspørsmål
            .undersporsmal?.first { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE" }

    var vedtaksperiode: List<Periode> = emptyList()
    var vedtakstype = false

    if (oppholdstillatelsePeriodeSpørsmål != null && oppholdstillatelsePeriodeSpørsmål.svar?.isNotEmpty() == true) {
        vedtaksperiode = mapBrukerSpørsmålDato(oppholdstillatelsePeriodeSpørsmål.svar)
        vedtakstype = false
    }

    return VedtaksType(
        erPermanentVedtaksType = vedtakstype,
        periode = vedtaksperiode,
    )
}

private fun mapOppholdstilateleBrukerSpørsmål_v2(
    oppholdstillatelseBrukerspørsmål: MedlemskapsBrukerSpørsmål
): MedlemskapOppholdstillatelseBrukerspørsmål? {
    val svar = mapSvar(oppholdstillatelseBrukerspørsmål.svar)
    val vedtakstype = permanentEllerMidlertidigVedtakstypeFraUnderspørsmålV2(oppholdstillatelseBrukerspørsmål)
    return if (svar) {
        val vedtaksdato = hentVedtaksdatoFraUnderspørsmål(oppholdstillatelseBrukerspørsmål.undersporsmal)
        MedlemskapOppholdstillatelseBrukerspørsmål(
            id = oppholdstillatelseBrukerspørsmål.id,
            spørsmalstekst = oppholdstillatelseBrukerspørsmål.sporsmalstekst,
            svar = svar,
            vedtaksdato = LocalDate.parse(vedtaksdato),
            vedtaksTypePermanent = vedtakstype.erPermanentVedtaksType,
            perioder = vedtakstype.periode,
        )
    } else {
        MedlemskapOppholdstillatelseBrukerspørsmål(
            id = oppholdstillatelseBrukerspørsmål.id,
            spørsmalstekst = oppholdstillatelseBrukerspørsmål.sporsmalstekst,
            svar = svar,
            vedtaksdato = LocalDate.now(),
            vedtaksTypePermanent = false,
            perioder = emptyList(),
        )
    }
}

private class VedtaksType(val erPermanentVedtaksType: Boolean, val periode: List<Periode>)