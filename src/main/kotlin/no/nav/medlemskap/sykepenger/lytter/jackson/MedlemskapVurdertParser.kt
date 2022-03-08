package no.nav.medlemskap.sykepenger.lytter.jackson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class MedlemskapVurdertParser {

    fun parse(jsonString: String): JsonNode {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return mapper.readValue(jsonString)

    }

    fun ToJson(obj: Any): JsonNode {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return mapper.valueToTree(obj)

    }
}