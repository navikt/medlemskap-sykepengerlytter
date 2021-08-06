package no.nav.medlemskap.sykepenger.lytter.jakson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.medlemskap.sykepenger.lytter.domain.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.domain.LovMeRequest
import no.nav.medlemskap.sykepenger.lytter.domain.Periode
import no.nav.medlemskap.sykepenger.lytter.domain.SykepengeSoknad


class JaksonParser {
    fun parse(jsonString: String): SykepengeSoknad {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return  mapper.readValue(jsonString)

    }
    fun createLovMeRequest(node:JsonNode): LovMeRequest?{
        val fnr = node.get("fnr").textValue()
        val fom = node.get("fom").textValue()
        val tom = node.get("tom").textValue()
        val arbeidUtland = false;
        return LovMeRequest(fnr,fom, Periode(fom,tom), Brukerinput(false))
    }


}