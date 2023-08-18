package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.saga.persistence.Brukersporsmaal
import no.nav.medlemskap.saga.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.saga.persistence.Medlemskap_oppholdstilatelse_brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
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
            val sporsmålArray = JsonNode.get("sporsmal")
            val fnr = JsonNode.get("fnr").asText()
            val status = JsonNode.get("status").asText()
            val type = JsonNode.get("type").asText()
            val id = JsonNode.get("id").asText()
            val sendtArbeidsgiver = JsonNode.get("sendtArbeidsgiver").asText(null)
            val sendtNav = JsonNode.get("sendtNav").asText(null)
            val sendtNavDato = parseDateString(sendtNav)
            val sendArbeidsgiverDato = parseDateString(sendtArbeidsgiver)


            val arbeidutland = sporsmålArray.find { it.get("tag").asText().equals("ARBEID_UTENFOR_NORGE") }

            val brukersp_arb_utland_old_model:FlexBrukerSporsmaal = FlexBrukerSporsmaalmapArbeidUtlandOldModel(arbeidutland)
            val mapper = BrukersporsmaalMapper(JsonNode)

            //val MEDLEMSKAP_OPPHOLDSTILLATELSE = sporsmålArray.find { it.get("tag").asText().equals("`medlemskap-oppholdstilatelse-json`") }

            //val medlemskapOppholdstilatelseBrukersporsmaal:Medlemskap_oppholdstilatelse_brukersporsmaal? = mapOppholdstilatele_BrukerSpørsmål(MEDLEMSKAP_OPPHOLDSTILLATELSE)
            return Brukersporsmaal(
                fnr,
                id,
                DatePicker().findEarliest(sendArbeidsgiverDato, sendtNavDato),
                "SYKEPENGER",
                status,
                brukersp_arb_utland_old_model,
                mapper.oppholdstilatelse_brukersporsmaal,
                null
            )

            /*
            val MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE = sporsmålArray.find { it.get("tag").asText().equals("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE") }

            val opphold = JacksonParser().parseFlexBrukerpSorsmaalV2(`medlemskap-oppholdstilatelse-json`!!.toPrettyString())

             */
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
