package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments

import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.*
import java.time.LocalDate

class BrukersporsmaalMapper(val rootNode: JsonNode) {
    companion object {
        private val secureLogger = KotlinLogging.logger("tjenestekall")


    }

    val sporsmålArray = rootNode.get("sporsmal")
    val oppholdstilatelse_brukersporsmaal = getOppholdstilatelse_brukerspørsmål()
    val arbeidutland = sporsmålArray.find { it.get("tag").asText().equals("ARBEID_UTENFOR_NORGE") }
    val arbeidutland_brukersporsmaal = sporsmålArray.find { it.get("tag").asText().equals("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE") }
    val oppholdUtenforNorge_brukersporsmaal = sporsmålArray.find { it.get("tag").asText().equals("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE") }
    val oppholdUtenforEOS_brukersporsmaal = sporsmålArray.find { it.get("tag").asText().equals("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS") }
    val brukersp_arb_utland_old_model: FlexBrukerSporsmaal = FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidutland)
    val arbeidUtlandBrukerSporsmaal = getarbeidUtlandBrukerSporsmaal()
    val oppholdUtenforNorge = getOppholdUtenforNorgeBrukerSporsmaal()
    val oppholdUtenforEOS = getOppholdUtenforEOSBrukerSporsmaal()

    private fun getOppholdUtenforEOSBrukerSporsmaal(): Medlemskap_opphold_utenfor_eos? {
        if (oppholdUtenforEOS_brukersporsmaal != null){
            return mapOppholdUtenforEOS_BrukerSporsmaal(oppholdUtenforEOS_brukersporsmaal)

        }
        return null
    }

    private fun mapOppholdUtenforEOS_BrukerSporsmaal(oppholdutenforEOS: JsonNode): Medlemskap_opphold_utenfor_eos? {
        val flexModel: FlexMedlemskapsBrukerSporsmaal = JacksonParser().toDomainObject(oppholdutenforEOS)
        val id = flexModel.id
        val sporsmalstekst = flexModel.sporsmalstekst
        val svar: Boolean = "JA" == flexModel.svar?.get(0)?.verdi ?: "NEI"
        var utlandsopphold: List<OppholdUtenforEOS> = emptyList()
        if (svar){
             utlandsopphold=
                mapOppholdUtenforEOS(flexModel.undersporsmal?.filter { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_GRUPPERING") }
                    ?: emptyList())
        }
        return Medlemskap_opphold_utenfor_eos(id,sporsmalstekst,svar,utlandsopphold);
    }


    private fun mapOppholdUtenforEOS(flex_OppholdUtenforEOS: List<FlexMedlemskapsBrukerSporsmaal>): List<OppholdUtenforEOS> {
        return flex_OppholdUtenforEOS.map{
            val hvor = it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_HVOR") }?.svar!!.first()!!.verdi
            val grunnNode = it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_BEGRUNNELSE") }
            val v2 = grunnNode?.undersporsmal?.filter { it.svar?.size ==1 }
            val grunn = v2?.first()!!.sporsmalstekst
            val periode:List<Periode> = listOf(JacksonParser().toDomainObject(it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_NAAR") }?.svar!!.first()!!.verdi))
            OppholdUtenforEOS(it.id,hvor,grunn!!,periode)
        }

    }

    private fun getOppholdUtenforNorgeBrukerSporsmaal(): Medlemskap_opphold_utenfor_norge ?{
        if (oppholdUtenforNorge_brukersporsmaal != null){
            return mapOppholdUtenforNorge_BrukerSporsmaal(oppholdUtenforNorge_brukersporsmaal)

        }
        return null
    }

    private fun mapOppholdUtenforNorge_BrukerSporsmaal(oppholdUtenforNorge: JsonNode): Medlemskap_opphold_utenfor_norge? {
        val flexModel: FlexMedlemskapsBrukerSporsmaal = JacksonParser().toDomainObject(oppholdUtenforNorge)
        val id = flexModel.id
        val sporsmalstekst = flexModel.sporsmalstekst
        val svar: Boolean = "JA" == flexModel.svar?.get(0)?.verdi ?: "NEI"
        var utlandsopphold: List<OppholdUtenforNorge> = emptyList()
        if (svar){
            utlandsopphold = mapOppholdUtenforNorge(flexModel.undersporsmal?.filter { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_GRUPPERING") }
                    ?: emptyList())

        }
        return Medlemskap_opphold_utenfor_norge(id,sporsmalstekst,svar,utlandsopphold);
    }



    private fun getarbeidUtlandBrukerSporsmaal(): Medlemskap_utfort_arbeid_utenfor_norge? {
        if (arbeidutland_brukersporsmaal != null) {
            return maputfortArbeidUtenforNorge_BrukerSpørsmål(arbeidutland_brukersporsmaal)

        } else {
            return null
        }

    }


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

    fun maputfortArbeidUtenforNorge_BrukerSpørsmål(arbeidutland: JsonNode): Medlemskap_utfort_arbeid_utenfor_norge? {
        try {
            val flexModel: FlexMedlemskapsBrukerSporsmaal = JacksonParser().toDomainObject(arbeidutland)
            val id = flexModel.id
            val sporsmalstekst = flexModel.sporsmalstekst
            val svar: Boolean = "JA" == flexModel.svar?.get(0)?.verdi ?: "NEI"
            var utlandsopphold: List<ArbeidUtenforNorge> = emptyList()
            if (svar){
                utlandsopphold  = mapArbeidUtenforNorge(flexModel.undersporsmal?.filter { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_GRUPPERING") }
                        ?: emptyList())
            }
            return Medlemskap_utfort_arbeid_utenfor_norge(
                id = id,
                sporsmalstekst = sporsmalstekst,
                svar = svar,
                utlandsopphold
            )
        } catch (e: Exception) {
            secureLogger.error(
                "Not able to parse Medlemskap_utfort_arbeid_utenfor_norge",
                StructuredArguments.kv("json", arbeidutland.toPrettyString())
            )
            return null
        }

    }

    private fun mapOppholdUtenforNorge(flex_OppholdUtenforNorge: List<FlexMedlemskapsBrukerSporsmaal>): List<OppholdUtenforNorge> {
        return flex_OppholdUtenforNorge.map{
            val hvor = it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_HVOR") }?.svar!!.first()!!.verdi
            val grunnNode = it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_BEGRUNNELSE") }
            val v2 = grunnNode?.undersporsmal?.filter { it.svar?.size ==1 }
            val grunn = v2?.first()!!.sporsmalstekst
            val periode:List<Periode> = listOf(JacksonParser().toDomainObject(it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_NAAR") }?.svar!!.first()!!.verdi))
            OppholdUtenforNorge(it.id,hvor,grunn!!,periode)
        }

    }

    private fun mapArbeidUtenforNorge(flex_arbeidUtenforNorgeList: List<FlexMedlemskapsBrukerSporsmaal>): List<ArbeidUtenforNorge> {
        val listOfArbeidUtenforNorge = flex_arbeidUtenforNorgeList.map {
            ArbeidUtenforNorge(
                it.id,
                it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_ARBEIDSGIVER") }?.svar!!.first()!!.verdi,
                it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_HVOR") }?.svar!!.first()!!.verdi,
                listOf(JacksonParser().toDomainObject(it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_NAAR") }?.svar!!.first()!!.verdi))
            )
        }
        return listOfArbeidUtenforNorge


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
            secureLogger.error(
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
            secureLogger.error(
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
