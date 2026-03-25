package no.nav.medlemskap.sykepenger.lytter.jackson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

class MedlemskapVurdertParser {

    companion object {
        private val mapper: ObjectMapper = JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .findAndAddModules()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .build()
    }

    fun parse(jsonString: String): JsonNode {
        return mapper.readValue(jsonString)
    }

    fun ToJson(obj: Any): JsonNode {
        return mapper.valueToTree(obj)
    }
}