package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.saga.persistence.*
import no.nav.medlemskap.sykepenger.lytter.config.retryConfig
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import java.time.LocalDate

class BrukersporsmaalMapper(val rootNode: JsonNode){
    companion object {
        private val log = KotlinLogging.logger { }
        private val secureLogger = KotlinLogging.logger { }


    }
    val sporsmålArray = rootNode.get("sporsmal")
    val oppholdstilatelse_brukersporsmaal = getOppholdstilatelse_brukerspørsmål()
    val arbeidutland = sporsmålArray.find { it.get("tag").asText().equals("ARBEID_UTENFOR_NORGE") }
    val arbeidutland_brukersporsmaal = sporsmålArray.find { it.get("tag").asText().equals("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE") }
    val brukersp_arb_utland_old_model:FlexBrukerSporsmaal = FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidutland)
    val arbeidUtlandBrukerSporsmaal = getarbeidUtlandBrukerSporsmaal()

    private fun getarbeidUtlandBrukerSporsmaal(): Medlemskap_utfort_arbeid_utenfor_norge? {
        if (arbeidutland_brukersporsmaal!=null){
            return maputfortArbeidUtenforNorge_BrukerSpørsmål(arbeidutland_brukersporsmaal)

        }
        else{
            return null
        }

    }


    fun getOppholdstilatelse_brukerspørsmål(): Medlemskap_oppholdstilatelse_brukersporsmaal?{
        val medlemskap_oppholdstilatelse_json = sporsmålArray.find { it.get("tag").asText().equals("MEDLEMSKAP_OPPHOLDSTILLATELSE") }
        if (medlemskap_oppholdstilatelse_json!=null){
            return mapOppholdstilatele_BrukerSpørsmål(medlemskap_oppholdstilatelse_json)

        }
        else{
            return null
        }

}
fun maputfortArbeidUtenforNorge_BrukerSpørsmål(arbeidutland: JsonNode): Medlemskap_utfort_arbeid_utenfor_norge? {
    val flexModel:FlexMedlemskapsBrukerSporsmaal = JacksonParser().toDomainObject(arbeidutland)
    val id = flexModel.id
    val sporsmalstekst = flexModel.sporsmalstekst
    val utlandsopphold:List<ArbeidUtenforNorge> = mapArbeidUtenforNorge(flexModel.undersporsmal?.filter { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_GRUPPERING") }
        ?: emptyList())
    val svar:Boolean = "JA" == flexModel.svar?.get(0)?.verdi ?:"NEI"
    return Medlemskap_utfort_arbeid_utenfor_norge(id = id,
        sporsmalstekst = sporsmalstekst,
        svar=svar,
        utlandsopphold)


}

    private fun mapArbeidUtenforNorge(flex_arbeidUtenforNorgeList: List<FlexMedlemskapsBrukerSporsmaal>): List<ArbeidUtenforNorge> {
        val listOfArbeidUtenforNorge = flex_arbeidUtenforNorgeList.map {
            ArbeidUtenforNorge(
                it.id,
                it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_ARBEIDSGIVER") }?.svar!!.first()!!.verdi,
                it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_HVOR") }?.svar!!.first()!!.verdi,
                listOf(JacksonParser().toDomainObject(it.undersporsmal?.find { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_NAAR") }?.svar!!.first()!!.verdi)))
        }
        return listOfArbeidUtenforNorge


    }



    fun mapOppholdstilatele_BrukerSpørsmål(medlemskapOppholdstillatelse: JsonNode): Medlemskap_oppholdstilatelse_brukersporsmaal? {
        try{
            val flexModel:FlexMedlemskapsBrukerSporsmaal = JacksonParser().toDomainObject(medlemskapOppholdstillatelse)
            val id = flexModel.id
            val sporsmalstekst = flexModel.sporsmalstekst
            val svar:Boolean = "JA" == flexModel.svar?.get(0)?.verdi ?:"NEI"
            val vedtaksdato = flexModel.undersporsmal?.filter { it.tag== "MEDLEMSKAP_OPPHOLDSTILLATELSE_VEDTAKSDATO" }?.first()?.svar?.first()?.verdi
            val permanentOpphold = flexModel.undersporsmal?.filter { it.tag== "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT" }?.first()?.svar?.first()?.verdi
            var perioder = mutableListOf<Periode>()
            if (permanentOpphold == "NEI"){
                val periode = flexModel.undersporsmal?.filter { it.tag== "MEDLEMSKAP_OPPHOLDSTILLATELSE_PERMANENT" }?.first()?.undersporsmal?.first()?.svar?.first()?.verdi
                val periodedto:Periode = JacksonParser().toDomainObject(periode!!)
                perioder.add(periodedto)
            }
            val response = Medlemskap_oppholdstilatelse_brukersporsmaal(
                id=id,
                sporsmalstekst=sporsmalstekst,
                svar=svar,
                vedtaksdato = LocalDate.parse(vedtaksdato),
                vedtaksTypePermanent = "JA" == permanentOpphold,
                perioder = perioder)
            return response
        }
        catch (e:Exception){
            secureLogger.error("Not able to parse Medlemskap_oppholdstilatelse_brukersporsmaal",
                StructuredArguments.kv("json", medlemskapOppholdstillatelse.toPrettyString()))       }
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