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

    val sporsmålArray = sporsmal
    val oppholdstilatelse_brukersporsmaal = getOppholdstilatelse_brukerspørsmål()
    val arbeidutland = sporsmålArray.find { it.get("tag").asText().equals("ARBEID_UTENFOR_NORGE") }
    val brukersp_arb_utland_old_model: FlexBrukerSporsmaal = FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidutland)
    val arbeidUtlandBrukerSporsmaal = getutfoertArbeidUtenforNorgeBrukerSporsmaal(utfoertArbeidUtenforNorge)
    val oppholdUtenforNorge = getOppholdUtenforNorgeBrukerSporsmaal(oppholdUtenforNorgeSpoersmaal)
    val oppholdUtenforEOS = getOppholdUtenforEOSBrukerSporsmaal(oppholdUtenforEOSSpoersmaal)

    fun getOppholdstilatelse_brukerspørsmål(): Medlemskap_oppholdstilatelse_brukersporsmaal? {
        val medlemskap_oppholdstilatelse_jsonv2 =
            sporsmålArray.find { it.get("tag").asText().equals("MEDLEMSKAP_OPPHOLDSTILLATELSE_V2") }
        if (medlemskap_oppholdstilatelse_jsonv2 != null) {
            return mapOppholdstilatele_BrukerSpørsmålv2(medlemskap_oppholdstilatelse_jsonv2)

        }
        val medlemskap_oppholdstilatelse_json =
            sporsmålArray.find { it.get("tag").asText().equals("MEDLEMSKAP_OPPHOLDSTILLATELSE") }
        if (medlemskap_oppholdstilatelse_json != null) {
            return mapOppholdstilatele_BrukerSpørsmål(medlemskap_oppholdstilatelse_json)

        }
        else {
            return null
        }

    }

    fun mapOppholdstilatele_BrukerSpørsmål(medlemskapOppholdstillatelse: JsonNode): Medlemskap_oppholdstilatelse_brukersporsmaal? {
        try {
            val flexModel: FlexMedlemskapsBrukerSporsmaal = JacksonParser().toDomainObject(medlemskapOppholdstillatelse)
            val id = flexModel.id
            val sporsmalstekst = flexModel.sporsmalstekst
            val svar: Boolean = "JA" == flexModel.svar?.get(0)?.verdi ?: "NEI"
            val vedtaksdato = flexModel.undersporsmal?.filter { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_VEDTAKSDATO" }
                ?.first()?.svar?.first()?.verdi
            val midlertidigEllerPermanentNode =
                flexModel.undersporsmal?.filter { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_GRUPPE" }?.first()
            val midlertidig = midlertidigEllerPermanentNode?.undersporsmal?.filter { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_MIDLERTIDIG" }?.first()
            val permanent = midlertidigEllerPermanentNode?.undersporsmal?.filter { it.tag == "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT" }?.first()

            var perioder = mutableListOf<Periode>()
            var vedtaksTypePermanent = ""
            if (midlertidig!=null && true == midlertidig.svar?.isNotEmpty()){
                val periode = midlertidig.undersporsmal?.first()?.svar!!.first()
                val periodedto: Periode = JacksonParser().toDomainObject(periode!!.verdi)
                perioder.add(periodedto)
                vedtaksTypePermanent = "NEI"
            }
            /*
            if (midlertidig != null && true == midlertidig.undersporsmal?.first()?.svar?.isNotEmpty()){
                    val periode = midlertidig.undersporsmal?.first()?.svar!!.first()
                    val periodedto: Periode = JacksonParser().toDomainObject(periode!!.verdi)
                perioder.add(periodedto)
                vedtaksTypePermanent = "NEI"
            }

             */
            if (permanent!=null && true == permanent.svar?.isNotEmpty()){
                vedtaksTypePermanent = "JA"
                val fomdato = permanent.undersporsmal?.first()?.svar!!.first()
                val fomLocalDate = LocalDate.parse(fomdato.verdi)
                perioder.add(Periode(fomLocalDate, LocalDate.MAX))

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
    fun mapOppholdstilatele_BrukerSpørsmålv2(medlemskapOppholdstillatelse: JsonNode): Medlemskap_oppholdstilatelse_brukersporsmaal? {
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

    fun FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidutland: JsonNode?): FlexBrukerSporsmaal {
        var svarText: String = "IKKE OPPGITT"
        var svar: Boolean?
        if (arbeidutland != null) {
            //println(arbeidutland)
            try {
                svarText = arbeidutland.get("svar").get(0).get("verdi").asText()
            } catch (t: Throwable) {

            }
        }
        if (svarText == "IKKE OPPGITT") {
            svar = null
        } else {
            if (svarText == "NEI") {
                svar = false
            } else if (svarText == "JA") {
                svar = true
            } else svar = null
        }
        return FlexBrukerSporsmaal(svar)
    }
}