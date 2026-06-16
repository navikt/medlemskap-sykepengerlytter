package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadsstatusDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.FlexMessageRecord
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SoknadRecordHandler
import org.slf4j.MarkerFactory

open class BehandleSykepengesoeknadHandler(
    private val soknadRecordHandler: SoknadRecordHandler
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun handleLovmeRequest(flexMessageRecord: FlexMessageRecord) {
        val requestObject = JacksonParser().parse(flexMessageRecord.value)
        if (soknadSkalSendesTeamLovMe(requestObject)){
            val soknadRecord = SoknadRecord(flexMessageRecord.partition, flexMessageRecord.offset, flexMessageRecord.value, flexMessageRecord.key, flexMessageRecord.topic, requestObject)
            soknadRecordHandler.handle(soknadRecord)
        }
        else{
            log.info(teamLogs, "melding filtrert ut da det ikke fyller kriterier for å bli sendt til regel motor",
                kv("x_data",JacksonParser().ToJson(requestObject).toPrettyString()),
                kv("callId", flexMessageRecord.key)
            )
        }
    }

    /*
    * SP1201 - Skal melding behandles av Lovme
    * */
    fun soknadSkalSendesTeamLovMe(lovmeSoknadDTO: LovmeSoknadDTO) =
        lovmeSoknadDTO.status == SoknadsstatusDTO.SENDT.name &&
                (
                lovmeSoknadDTO.type == SoknadstypeDTO.ARBEIDSTAKERE ||
                lovmeSoknadDTO.type == SoknadstypeDTO.GRADERT_REISETILSKUDD
                ) &&
                false == lovmeSoknadDTO.ettersending
}
