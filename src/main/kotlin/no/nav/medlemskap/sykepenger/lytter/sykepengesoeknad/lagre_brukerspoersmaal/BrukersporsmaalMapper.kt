package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstilatelseBrukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapBrukerSpoersmaalDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapSvar
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.getutfoertArbeidUtenforNorgeBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.getOppholdUtenforEOSBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.getOppholdUtenforNorgeBrukerSporsmaal
import java.time.LocalDate

class BrukersporsmaalMapper(sporsmal: JsonNode) {
    val spoersmaalListe: List<FlexMedlemskapsBrukerSporsmaal> = objectMapper.convertValue(sporsmal)

    val arbeidUtland = spoersmaalListe.find { it.tag == "ARBEID_UTENFOR_NORGE" }
    val utfoertArbeidUtenforNorge = spoersmaalListe.find { it.tag == "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE" }
    val oppholdUtenforNorgeSpoersmaal = spoersmaalListe.find { it.tag == "MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE" }
    val oppholdUtenforEOSSpoersmaal = spoersmaalListe.find { it.tag == "MEDLEMSKAP_OPPHOLD_UTENFOR_EOS" }

    val oppholdstilatelse_brukersporsmaal = getOppholdstilatelse_brukerspørsmål(spoersmaalListe)
    val brukersp_arb_utland_old_model: FlexBrukerSporsmaal = FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidUtland)
    val arbeidUtlandBrukerSporsmaal = getutfoertArbeidUtenforNorgeBrukerSporsmaal(utfoertArbeidUtenforNorge)
    val oppholdUtenforNorge = getOppholdUtenforNorgeBrukerSporsmaal(oppholdUtenforNorgeSpoersmaal)
    val oppholdUtenforEOS = getOppholdUtenforEOSBrukerSporsmaal(oppholdUtenforEOSSpoersmaal)

    fun FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidutland: FlexMedlemskapsBrukerSporsmaal?): FlexBrukerSporsmaal {
        var svar: Boolean? = null
        if (arbeidutland?.svar != null)
            svar = mapSvar(arbeidutland.svar)
        return FlexBrukerSporsmaal(svar)
    }


    fun getOppholdstilatelse_brukerspørsmål(spoersmaalListe: List<FlexMedlemskapsBrukerSporsmaal>): MedlemskapOppholdstilatelseBrukersporsmaal? {
        val oppholdstillatelseBrukerspoersmaal_v2 = spoersmaalListe.find { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_V2" }
        val oppholdstillatelseBrukerspoersmaal = spoersmaalListe.find { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE" }

        return if(oppholdstillatelseBrukerspoersmaal_v2 != null)
            mapOppholdstilatele_BrukerSpørsmålv2(oppholdstillatelseBrukerspoersmaal_v2)
        else if (oppholdstillatelseBrukerspoersmaal != null)
            mapOppholdstilatele_BrukerSporsmaal(oppholdstillatelseBrukerspoersmaal)
        else null
    }

    private fun hentVedtaksdatoFraUndersporsmaal(undersporsmal: List<FlexMedlemskapsBrukerSporsmaal>?): String {
        return undersporsmal?.filter { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_VEDTAKSDATO" }?.first()?.svar?.first()?.verdi ?: "null"
    }

    class VedtaksType(val erPermanentVedtaksType: Boolean, val periode: List<Periode>)

    private fun permanentEllerMidlertidigVedtaksTypeFraUndersporsmaal(undersporsmal: List<FlexMedlemskapsBrukerSporsmaal>?): VedtaksType {
        val oppholdstillatelseSporsmaalGruppering = undersporsmal?.filter { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_GRUPPE" }?.first()

        val oppholdstillatelseMidlertidigSporsmaal =
            oppholdstillatelseSporsmaalGruppering
                ?.undersporsmal?.first { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_MIDLERTIDIG" }

        val oppholdstillatelsePermanentBrukersporsmaal =
            oppholdstillatelseSporsmaalGruppering
                ?.undersporsmal?.first { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT" }

        var erVedtakstypePermanent = false
        var periode: List<Periode> = emptyList()

        if (oppholdstillatelsePermanentBrukersporsmaal != null && oppholdstillatelsePermanentBrukersporsmaal.svar?.isNotEmpty() == true) {
            erVedtakstypePermanent = true
            val fom = LocalDate.parse(oppholdstillatelsePermanentBrukersporsmaal.svar.first().verdi)
            periode = listOf(Periode(fom, LocalDate.MAX))
        }

        if (oppholdstillatelseMidlertidigSporsmaal != null && oppholdstillatelseMidlertidigSporsmaal.svar?.isNotEmpty() == true) {
            erVedtakstypePermanent = true
            val fom = LocalDate.parse(oppholdstillatelseMidlertidigSporsmaal.svar.first().verdi)
            periode = listOf(Periode(fom, LocalDate.MAX))
        }

        return VedtaksType(
            erPermanentVedtaksType = erVedtakstypePermanent,
            periode = periode
        )
    }

    fun mapOppholdstilatele_BrukerSporsmaal(oppholdstillatelseBrukersporsmaal: FlexMedlemskapsBrukerSporsmaal): MedlemskapOppholdstilatelseBrukersporsmaal? {
        val vedtaksdato = hentVedtaksdatoFraUndersporsmaal(oppholdstillatelseBrukersporsmaal.undersporsmal)
        val vedtakstype = permanentEllerMidlertidigVedtaksTypeFraUndersporsmaal(oppholdstillatelseBrukersporsmaal.undersporsmal)
        return MedlemskapOppholdstilatelseBrukersporsmaal(
            id = oppholdstillatelseBrukersporsmaal.id,
            sporsmalstekst = oppholdstillatelseBrukersporsmaal.sporsmalstekst,
            svar = mapSvar(oppholdstillatelseBrukersporsmaal.svar),
            vedtaksdato = LocalDate.parse(vedtaksdato),
            vedtaksTypePermanent = vedtakstype.erPermanentVedtaksType,
            perioder = vedtakstype.periode
        )
    }

    private fun permanentEllerMidlertidigVedtakstypeFraUndersporsmaal_v2(oppholdstillatelseBrukersporsmaal: FlexMedlemskapsBrukerSporsmaal): VedtaksType {
        val oppholdstillatelsePeriodeSporsmaal =
            oppholdstillatelseBrukersporsmaal
            .undersporsmal?.first{ it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE" }

        var vedtaksperiode: List<Periode> = emptyList()
        var vedtakstype = false

        if (oppholdstillatelsePeriodeSporsmaal != null && oppholdstillatelsePeriodeSporsmaal.svar?.isNotEmpty() == true) {
            vedtaksperiode = mapBrukerSpoersmaalDato(oppholdstillatelsePeriodeSporsmaal.svar)
            vedtakstype = false
        }

        return VedtaksType(
            erPermanentVedtaksType = vedtakstype,
            periode = vedtaksperiode
        )
    }

    fun mapOppholdstilatele_BrukerSpørsmålv2(oppholdstillatelseBrukersporsmaal: FlexMedlemskapsBrukerSporsmaal): MedlemskapOppholdstilatelseBrukersporsmaal? {
        val svar = mapSvar(oppholdstillatelseBrukersporsmaal.svar)
        val vedtakstype = permanentEllerMidlertidigVedtakstypeFraUndersporsmaal_v2(oppholdstillatelseBrukersporsmaal)
        return if (svar) {
            val vedtaksdato = hentVedtaksdatoFraUndersporsmaal(oppholdstillatelseBrukersporsmaal.undersporsmal)
            MedlemskapOppholdstilatelseBrukersporsmaal(
                id = oppholdstillatelseBrukersporsmaal.id,
                sporsmalstekst = oppholdstillatelseBrukersporsmaal.sporsmalstekst,
                svar = svar,
                vedtaksdato = LocalDate.parse(vedtaksdato),
                vedtaksTypePermanent = vedtakstype.erPermanentVedtaksType,
                perioder = vedtakstype.periode
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
}