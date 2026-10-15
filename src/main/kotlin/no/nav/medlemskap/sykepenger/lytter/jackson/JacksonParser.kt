package no.nav.medlemskap.sykepenger.lytter.jackson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv

import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag


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

    fun parse(jsonString: String): SykepengesoeknadGrunnlag? {
        try {
            return mapper.readValue(jsonString)
        } catch (t: JsonProcessingException) {
            val melding = when (t) {
                is InvalidFormatException ->
                    if (t.targetType.isEnum) {
                        "Kan ikke lese sykepengesøknad: ukjent enumverdi for ${t.targetType.simpleName}"
                    } else {
                        "Kan ikke lese sykepengesøknad: ugyldig verdi for ${t.targetType.simpleName}"
                    }
                is JsonParseException -> "Kan ikke lese sykepengesøknad: ugyldig JSON-syntaks"
                is MismatchedInputException ->
                    "Kan ikke lese sykepengesøknad: manglende påkrevd felt eller feil JSON-struktur"
                else -> "Kan ikke lese sykepengesøknad: JSON kan ikke deserialiseres"
            }
            log.warn(
                melding,
                kv("feiltype", t.javaClass.simpleName),
                kv("linje", t.location?.lineNr),
                kv("kolonne", t.location?.columnNr)
            )
            return null
        }
    }

    fun lesSykepengesøknadGrunnlag(jsonString: String): SykepengesoeknadGrunnlag? =
        parse(jsonString)


    fun parse(obj: Any): String {
        try {
            return mapper.writeValueAsString(obj)
        } catch (t: JsonProcessingException) {
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