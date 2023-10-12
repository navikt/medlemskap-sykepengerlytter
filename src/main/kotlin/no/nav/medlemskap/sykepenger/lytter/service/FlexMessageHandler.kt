package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv

import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import java.time.LocalDate
import java.time.LocalDateTime

open class FlexMessageHandler (
    private val configuration: Configuration,
    private val persistenceService: PersistenceService,
    private val soknadRecordHandler: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val secureLogger = KotlinLogging.logger { }


    }

    suspend fun handle(flexMessageRecord: FlexMessageRecord) {
        val requestObject = JacksonParser().parse(flexMessageRecord.value)
        secureLogger.info("mapping fnr to messageID. messageID ${flexMessageRecord.key} is regarding ${requestObject.fnr}",)
        if  (requestObject.type == SoknadstypeDTO.ARBEIDSTAKERE){
            handleBrukerSporsmaal(flexMessageRecord)
            handleLovmeRequest(flexMessageRecord)
        }
        else{
            log.info("Melding med id ${flexMessageRecord.key} filtrert ut. Ikke ønsket meldingstype : ${requestObject.type.name}")
        }


    }

     suspend fun handleLovmeRequest(flexMessageRecord: FlexMessageRecord) {
        val requestObject = JacksonParser().parse(flexMessageRecord.value)
        if (soknadSkalSendesTeamLovMe(requestObject)){
            val soknadRecord =SoknadRecord(flexMessageRecord.partition,flexMessageRecord.offset,flexMessageRecord.value,flexMessageRecord.key,flexMessageRecord.topic,requestObject)
            soknadRecordHandler.handle(soknadRecord)
        }
         else{
            secureLogger.info("melding filtrert ut da det ikke fyller kriterier for å bli sendt til regel motor",
                kv("x_data",JacksonParser().ToJson(requestObject).toPrettyString()),
                kv("callId", flexMessageRecord.key)
            )
        }
    }

    fun soknadSkalSendesTeamLovMe(lovmeSoknadDTO: LovmeSoknadDTO) =
        lovmeSoknadDTO.status == SoknadsstatusDTO.SENDT.name &&
                lovmeSoknadDTO.type == SoknadstypeDTO.ARBEIDSTAKERE &&
                false == lovmeSoknadDTO.ettersending

    private fun handleBrukerSporsmaal(flexMessageRecord: FlexMessageRecord) {
        val brukersporsmaal: Brukersporsmaal = mapMessage(flexMessageRecord)

        if (brukersporsmaal.status == Soknadstatus.SENDT.toString()) {
            val existing = persistenceService.hentbrukersporsmaalForSoknadID(brukersporsmaal.soknadid)
            if (existing != null){
                log.info(
                    "Flex melding for søknad ${flexMessageRecord.key}, " +
                            "offset : ${flexMessageRecord.offset}, " +
                            "partition : ${flexMessageRecord.partition}," +
                            "filtrert ut. duplikat melding: ${brukersporsmaal.soknadid}"
                )
                return
            }
            persistenceService.lagreBrukersporsmaal(brukersporsmaal)
            log.info(
                "Brukerspørsmål for søknad ${flexMessageRecord.key} lagret til databasen",
                kv("callId", flexMessageRecord.key),
                kv("dato", brukersporsmaal.eventDate)
            )
        } else {
            log.info(
                "Flex melding for søknad ${flexMessageRecord.key}, " +
                        "offset : ${flexMessageRecord.offset}, " +
                        "partition : ${flexMessageRecord.partition}," +
                        "filtrert ut. Feil status : ${brukersporsmaal.status}"
            )
        }
    }

    open fun mapMessage(flexMessageRecord: FlexMessageRecord): Brukersporsmaal {
        try {
            val json = flexMessageRecord.value
            val JsonNode = ObjectMapper().readTree(json)
            val fnr = JsonNode.get("fnr").asText()
            val status = JsonNode.get("status").asText()
            val id = JsonNode.get("id").asText()
            val sendtArbeidsgiver = JsonNode.get("sendtArbeidsgiver").asText(null)
            val sendtNav = JsonNode.get("sendtNav").asText(null)
            val sendtNavDato = parseDateString(sendtNav)
            val sendArbeidsgiverDato = parseDateString(sendtArbeidsgiver)
            // de som ikke har status SENDT skal ikke mappe bruker spørsmål da disse ikke er komplette
            if (status != Soknadstatus.SENDT.toString()){

                return Brukersporsmaal(
                    fnr,
                    id,
                    DatePicker().findEarliest(sendArbeidsgiverDato, sendtNavDato),
                    "SYKEPENGER",
                    status,
                    null,
                    null,
                    null
                )
            }
            val mapper = BrukersporsmaalMapper(JsonNode)

            return Brukersporsmaal(
                fnr,
                id,
                DatePicker().findEarliest(sendArbeidsgiverDato, sendtNavDato),
                "SYKEPENGER",
                status,
                mapper.brukersp_arb_utland_old_model,
                mapper.oppholdstilatelse_brukersporsmaal,
                mapper.arbeidUtlandBrukerSporsmaal,
                mapper.oppholdUtenforNorge,
                mapper.oppholdUtenforEOS
            )
        }
        catch (t:Throwable){
            log.error("not able to parse message ${t.message}, cause : ${t.cause}")
            secureLogger.error("not able to parse message ${t.message}", kv("body",flexMessageRecord.value))
            throw t
        }
    }

    private fun parseDateString(dateString: String?): LocalDate? {
        return try {
            LocalDateTime.parse(dateString).toLocalDate()
        } catch (t: Throwable) {
            null
        }
    }

}

open class DatePicker(){
    companion object {
        private val log = KotlinLogging.logger { }
        private val secureLogger = KotlinLogging.logger { }

    }
    open fun findEarliest(sendArbeidsgiverDato: LocalDate?, sendtNavDato: LocalDate?): LocalDate {
        if (sendArbeidsgiverDato == null  && sendtNavDato == null){
            return LocalDate.now()
        }
        if (sendArbeidsgiverDato==null && sendtNavDato!=null){
            return sendtNavDato
        }
        if (sendtNavDato==null && sendArbeidsgiverDato !=null){
            return sendArbeidsgiverDato
        }
        if (sendtNavDato!!.isBefore(sendArbeidsgiverDato)){
            return sendtNavDato
        }
        else{
            return sendArbeidsgiverDato!!
        }

    }
}
