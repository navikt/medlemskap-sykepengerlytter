package no.nav.medlemskap.sykepenger.lytter.jackson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import mu.KotlinLogging
import no.nav.medlemskap.saga.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.saga.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.rest.FlexVurderingRespons
import java.time.LocalDate
import java.time.LocalDateTime


class JacksonParser {
    private val log = KotlinLogging.logger { }
    fun parse(jsonString: String): LovmeSoknadDTO {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            return LovmeSoknadDTO(
                "",
                SoknadstypeDTO.ARBEIDSTAKERE,
                SoknadsstatusDTO.SENDT.name,
                "", null,
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDate.now(),
                LocalDate.now(), false
            )
        }
    }

    fun parseFlexVurdering(jsonString: String): FlexVurderingRespons {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            return FlexVurderingRespons("","","",LocalDate.now(),LocalDate.now(),"UAVKLART")
        }
    }
    fun parseFlexBrukerSporsmaal(jsonString: String): FlexBrukerSporsmaal {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            return FlexBrukerSporsmaal(null)
        }
    }
    fun parseFlexBrukerSporsmaalV2(jsonString: String): FlexMedlemskapsBrukerSporsmaal {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }

    fun parseMedlemskap(jsonString: String): Medlemskap {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }

    fun parseMedlemskap(medlemskap: Medlemskap): String {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.writeValueAsString(medlemskap)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }
    fun parseFlexBrukerSporsmaal(flexBrukerSporsmaal: FlexBrukerSporsmaal): String {
            try {
                val mapper: ObjectMapper = ObjectMapper()
                    .registerKotlinModule()
                    .findAndRegisterModules()
                    .configure(SerializationFeature.INDENT_OUTPUT, true)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                return mapper.writeValueAsString(flexBrukerSporsmaal)
            } catch (t: Throwable) {
                log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
                throw t;
            }
        }
    fun parse(obj: Any): String {
        try {
            val mapper: ObjectMapper = ObjectMapper()
                .registerKotlinModule()
                .findAndRegisterModules()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            return mapper.writeValueAsString(obj)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }
    fun ToJson(obj: Any): JsonNode {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return  mapper.valueToTree(obj);

    }
    fun ToJson(string: String): JsonNode {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return  mapper.readTree(string);

    }

    inline fun <reified T> toDomainObject(jsonNode: JsonNode): T {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return mapper.treeToValue(jsonNode)

    }
    inline fun <reified T> toDomainObject(jsonString: String): T {
        val mapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        return mapper.readValue<T>(jsonString)

    }
}