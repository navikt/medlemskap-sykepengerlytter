package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Status
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadRecord
import org.slf4j.MarkerFactory
import java.time.LocalDate
import java.time.LocalDateTime

class BrukersvarMapper {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    fun mapMessage(sykepengesoeknadRecord: SykepengesoeknadRecord): Brukersporsmaal {
        try {
            val json = sykepengesoeknadRecord.value
            val JsonNode = ObjectMapper().readTree(json)
            val fnr = JsonNode.get("fnr").asText()
            val status = JsonNode.get("status").asText()
            val id = JsonNode.get("id").asText()
            val sendtArbeidsgiver = JsonNode.get("sendtArbeidsgiver").asText(null)
            val dodsdato = JsonNode.get("dodsdato").asText(null)
            val sendtNav = JsonNode.get("sendtNav").asText(null)
            val sendtNavDato = parseDateString(sendtNav)
            val sendArbeidsgiverDato = parseDateString(sendtArbeidsgiver)
            // de som ikke har status SENDT skal ikke mappe bruker spørsmål da disse ikke er komplette
            if (status != Status.SENDT.toString()){

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
            //dersom bruker er død er alle brukerspørsmål ikke oppgitt.

            if (dodsdato!=null){
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
            log.error(teamLogs, "not able to parse message ${t.message}", kv("body",sykepengesoeknadRecord.value))
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
