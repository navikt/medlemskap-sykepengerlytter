package no.nav.medlemskap.sykepenger.lytter.jakson

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadsstatusDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO
import no.nav.medlemskap.sykepenger.lytter.service.LovMeService
import java.time.LocalDate
import java.time.LocalDateTime


class JaksonParser {
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
            return  mapper.readValue(jsonString)
        }
        catch (t:Throwable){
            log.error("Unable to parse json. Dropping message. Cause : ${t.message}")
            return LovmeSoknadDTO("",
                SoknadstypeDTO.ARBEIDSTAKERE,SoknadsstatusDTO.SENDT,
                "",null,
                LocalDate.now(),
                LocalDateTime.now(),
                LocalDate.now(),
                LocalDate.now(),false)
        }
    }

}