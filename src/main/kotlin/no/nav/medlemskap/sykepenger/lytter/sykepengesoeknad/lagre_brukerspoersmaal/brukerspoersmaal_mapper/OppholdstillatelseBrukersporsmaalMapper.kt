package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstilatelseBrukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapBrukerSpoersmaalDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapSvar
import java.time.LocalDate

fun getOppholdstillatelseBrukersporsmaal(
    oppholdstillatelseBrukerspoersmaalV2: FlexMedlemskapsBrukerSporsmaal?,
    oppholdstillatelseBrukerspoersmaal: FlexMedlemskapsBrukerSporsmaal?,
): MedlemskapOppholdstilatelseBrukersporsmaal? {
    return if (oppholdstillatelseBrukerspoersmaalV2 != null) {
        mapOppholdstilateleBrukerSpørsmål_v2(oppholdstillatelseBrukerspoersmaalV2)
    } else if (oppholdstillatelseBrukerspoersmaal != null) {
        mapOppholdstilateleBrukerSporsmaal(oppholdstillatelseBrukerspoersmaal)
    } else {
        null
    }
}

private fun hentVedtaksdatoFraUndersporsmaal(undersporsmal: List<FlexMedlemskapsBrukerSporsmaal>?): String {
    return undersporsmal?.first { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_VEDTAKSDATO" }?.svar?.first()?.verdi
        ?: "null"
}

private fun permanentEllerMidlertidigVedtaksTypeFraUndersporsmaal(
    undersporsmal: List<FlexMedlemskapsBrukerSporsmaal>?
): VedtaksType {
    val oppholdstillatelseSporsmaalGruppering =
        undersporsmal?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_GRUPPE" }

    if (oppholdstillatelseSporsmaalGruppering == null) {
        val oppholdstillatelsePermanentBrukersporsmaal =
            undersporsmal?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT" }
                ?: return VedtaksType(erPermanentVedtaksType = false, periode = emptyList())

        val erVedtakstypePermanent = mapSvar(oppholdstillatelsePermanentBrukersporsmaal.svar)
        val periodeSporsmaal = oppholdstillatelsePermanentBrukersporsmaal.undersporsmal
            ?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE" }

        return VedtaksType(
            erPermanentVedtaksType = erVedtakstypePermanent,
            periode = mapBrukerSpoersmaalDato(periodeSporsmaal?.svar),
        )
    }

    val oppholdstillatelseMidlertidigSporsmaal =
        oppholdstillatelseSporsmaalGruppering
            .undersporsmal?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_MIDLERTIDIG" }

    val oppholdstillatelsePermanentBrukersporsmaal =
        oppholdstillatelseSporsmaalGruppering
            .undersporsmal?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT" }

    var erVedtakstypePermanent = false
    var periode: List<Periode> = emptyList()

    if (oppholdstillatelsePermanentBrukersporsmaal != null && oppholdstillatelsePermanentBrukersporsmaal.svar?.isNotEmpty() == true) {
        erVedtakstypePermanent = true
        val fom = LocalDate.parse(
            oppholdstillatelsePermanentBrukersporsmaal.undersporsmal
                ?.first { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT_DATO" }?.svar?.first()?.verdi
        )
        periode = listOf(Periode(fom, LocalDate.MAX))
    }

    if (oppholdstillatelseMidlertidigSporsmaal != null && oppholdstillatelseMidlertidigSporsmaal.svar?.isNotEmpty() == true) {
        erVedtakstypePermanent = false
        val periodeSporsmaal = oppholdstillatelseMidlertidigSporsmaal.undersporsmal
            ?.firstOrNull { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_MIDLERTIDIG_PERIODE" }
        periode = mapBrukerSpoersmaalDato(periodeSporsmaal?.svar)
    }

    return VedtaksType(
        erPermanentVedtaksType = erVedtakstypePermanent,
        periode = periode,
    )
}

private fun mapOppholdstilateleBrukerSporsmaal(
    oppholdstillatelseBrukersporsmaal: FlexMedlemskapsBrukerSporsmaal
): MedlemskapOppholdstilatelseBrukersporsmaal {
    val vedtaksdato = hentVedtaksdatoFraUndersporsmaal(oppholdstillatelseBrukersporsmaal.undersporsmal)
    val vedtakstype =
        permanentEllerMidlertidigVedtaksTypeFraUndersporsmaal(oppholdstillatelseBrukersporsmaal.undersporsmal)
    return MedlemskapOppholdstilatelseBrukersporsmaal(
        id = oppholdstillatelseBrukersporsmaal.id,
        sporsmalstekst = oppholdstillatelseBrukersporsmaal.sporsmalstekst,
        svar = mapSvar(oppholdstillatelseBrukersporsmaal.svar),
        vedtaksdato = LocalDate.parse(vedtaksdato),
        vedtaksTypePermanent = vedtakstype.erPermanentVedtaksType,
        perioder = vedtakstype.periode,
    )
}

private fun permanentEllerMidlertidigVedtakstypeFraUndersporsmaalV2(
    oppholdstillatelseBrukersporsmaal: FlexMedlemskapsBrukerSporsmaal
): VedtaksType {
    val oppholdstillatelsePeriodeSporsmaal =
        oppholdstillatelseBrukersporsmaal
            .undersporsmal?.first { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE" }

    var vedtaksperiode: List<Periode> = emptyList()
    var vedtakstype = false

    if (oppholdstillatelsePeriodeSporsmaal != null && oppholdstillatelsePeriodeSporsmaal.svar?.isNotEmpty() == true) {
        vedtaksperiode = mapBrukerSpoersmaalDato(oppholdstillatelsePeriodeSporsmaal.svar)
        vedtakstype = false
    }

    return VedtaksType(
        erPermanentVedtaksType = vedtakstype,
        periode = vedtaksperiode,
    )
}

private fun mapOppholdstilateleBrukerSpørsmål_v2(
    oppholdstillatelseBrukersporsmaal: FlexMedlemskapsBrukerSporsmaal
): MedlemskapOppholdstilatelseBrukersporsmaal? {
    val svar = mapSvar(oppholdstillatelseBrukersporsmaal.svar)
    val vedtakstype = permanentEllerMidlertidigVedtakstypeFraUndersporsmaalV2(oppholdstillatelseBrukersporsmaal)
    return if (svar) {
        val vedtaksdato = hentVedtaksdatoFraUndersporsmaal(oppholdstillatelseBrukersporsmaal.undersporsmal)
        MedlemskapOppholdstilatelseBrukersporsmaal(
            id = oppholdstillatelseBrukersporsmaal.id,
            sporsmalstekst = oppholdstillatelseBrukersporsmaal.sporsmalstekst,
            svar = svar,
            vedtaksdato = LocalDate.parse(vedtaksdato),
            vedtaksTypePermanent = vedtakstype.erPermanentVedtaksType,
            perioder = vedtakstype.periode,
        )
    } else {
        MedlemskapOppholdstilatelseBrukersporsmaal(
            id = oppholdstillatelseBrukersporsmaal.id,
            sporsmalstekst = oppholdstillatelseBrukersporsmaal.sporsmalstekst,
            svar = svar,
            vedtaksdato = LocalDate.now(),
            vedtaksTypePermanent = false,
            perioder = emptyList(),
        )
    }
}

private class VedtaksType(val erPermanentVedtaksType: Boolean, val periode: List<Periode>)