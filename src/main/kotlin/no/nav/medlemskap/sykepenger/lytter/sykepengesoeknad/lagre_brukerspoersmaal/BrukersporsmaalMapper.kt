package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_oppholdstilatelse_brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapSvar
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.getutfoertArbeidUtenforNorgeBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.getOppholdUtenforEOSBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.getOppholdUtenforNorgeBrukerSporsmaal
import org.slf4j.MarkerFactory
import java.time.LocalDate

class BrukersporsmaalMapper(sporsmal: JsonNode) {
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    private val log  = KotlinLogging.logger { }

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


    fun getOppholdstilatelse_brukerspørsmål(spoersmaalListe: List<FlexMedlemskapsBrukerSporsmaal>): Medlemskap_oppholdstilatelse_brukersporsmaal? {
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

    fun mapOppholdstilatele_BrukerSporsmaal(medlemskapOppholdstillatelse: FlexMedlemskapsBrukerSporsmaal): Medlemskap_oppholdstilatelse_brukersporsmaal? {
        val vedtaksdato = hentVedtaksdatoFraUndersporsmaal(medlemskapOppholdstillatelse.undersporsmal)
        val vedtakstype = permanentEllerMidlertidigVedtaksTypeFraUndersporsmaal(medlemskapOppholdstillatelse.undersporsmal)
        return Medlemskap_oppholdstilatelse_brukersporsmaal(
            id = medlemskapOppholdstillatelse.id,
            sporsmalstekst = medlemskapOppholdstillatelse.sporsmalstekst,
            svar = mapSvar(medlemskapOppholdstillatelse.svar),
            vedtaksdato = LocalDate.parse(vedtaksdato),
            vedtaksTypePermanent = vedtakstype.erPermanentVedtaksType,
            perioder = vedtakstype.periode
        )
    }

    fun mapOppholdstilatele_BrukerSpørsmålv2(medlemskapOppholdstillatelse: FlexMedlemskapsBrukerSporsmaal): Medlemskap_oppholdstilatelse_brukersporsmaal? {
        try {
            val flexModel: FlexMedlemskapsBrukerSporsmaal = JacksonParser().toDomainObject(medlemskapOppholdstillatelse)
            val id = flexModel.id
            val sporsmalstekst = flexModel.sporsmalstekst
            val svar: Boolean = "JA" == flexModel.svar?.get(0)?.verdi ?: "NEI"
            //Bruker har svart NEI på oppholdstilatelse
            if (!svar)
            {
               return Medlemskap_oppholdstilatelse_brukersporsmaal(
                   id = id,
                   sporsmalstekst = sporsmalstekst,
                   svar = svar,
                   vedtaksdato = LocalDate.now(),
                   vedtaksTypePermanent = false,
                   perioder = emptyList()
               )
            }
            val vedtaksdato = flexModel.undersporsmal?.filter { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_VEDTAKSDATO" }
                ?.first()?.svar?.first()?.verdi
            val periode = flexModel.undersporsmal?.filter { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERIODE" }?.first()

            var perioder = mutableListOf<Periode>()
            var vedtaksTypePermanent = ""
            if (periode!=null && true == periode.svar?.isNotEmpty()){
                val periode = periode.svar!!.first()
                val periodedto: Periode = JacksonParser().toDomainObject(periode!!.verdi)
                perioder.add(periodedto)
                vedtaksTypePermanent = "NEI"
            }


            val response = Medlemskap_oppholdstilatelse_brukersporsmaal(
                id = id,
                sporsmalstekst = sporsmalstekst,
                svar = svar,
                vedtaksdato = LocalDate.parse(vedtaksdato),
                vedtaksTypePermanent = "JA" == vedtaksTypePermanent,
                perioder = perioder
            )
            return response
        } catch (e: Exception) {
            log.error(
                teamLogs,
                "Not able to parse Medlemskap_oppholdstilatelse_brukersporsmaal",
                StructuredArguments.kv("json", medlemskapOppholdstillatelse.toPrettyString())
            )
            return null
        }
    }
}