package no.nav.medlemskap.sykepenger.lytter.jackson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import mu.KotlinLogging

import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.rest.FlexVurderingRespons
import java.time.LocalDate
import java.time.LocalDateTime


class JacksonParser {
    private val log = KotlinLogging.logger { }

    companion object {
        @PublishedApi
        internal val mapper: ObjectMapper = JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .findAndAddModules()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .build()
    }

    fun parse(jsonString: String): LovmeSoknadDTO {
        try {
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
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            return FlexVurderingRespons("","","",LocalDate.now(),LocalDate.now(),"UAVKLART")
        }
    }
    fun parseFlexBrukerSporsmaal(jsonString: String): FlexBrukerSporsmaal {
        try {
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            return FlexBrukerSporsmaal(null)
        }
    }
    fun parseFlexBrukerSporsmaalV2(jsonString: String): FlexMedlemskapsBrukerSporsmaal {
        try {
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }

    fun parseMedlemskap(jsonString: String): Medlemskap {
        try {
            return mapper.readValue(jsonString)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }

    fun parseMedlemskap(medlemskap: Medlemskap): String {
        try {
            return mapper.writeValueAsString(medlemskap)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }
    fun parseFlexBrukerSporsmaal(flexBrukerSporsmaal: FlexBrukerSporsmaal): String {
            try {
                return mapper.writeValueAsString(flexBrukerSporsmaal)
            } catch (t: Throwable) {
                log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
                throw t;
            }
        }
    fun parse(obj: Any): String {
        try {
            return mapper.writeValueAsString(obj)
        } catch (t: Throwable) {
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            throw t;
        }
    }
    fun ToJson(obj: Any): JsonNode {
        return mapper.valueToTree(obj);
    }
    fun ToJson(string: String): JsonNode {
        return mapper.readTree(string);
    }

    inline fun <reified T> toDomainObject(jsonNode: JsonNode): T {
        return mapper.treeToValue(jsonNode)
    }
    inline fun <reified T> toDomainObject(jsonString: String): T {
        return mapper.readValue<T>(jsonString)
    }
}