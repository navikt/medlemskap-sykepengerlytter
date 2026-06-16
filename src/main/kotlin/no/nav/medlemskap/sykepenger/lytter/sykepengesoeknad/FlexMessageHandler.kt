package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv

import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.BehandleSykepengesoeknadHandler
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.brukersvar.BrukersvarHandler
import org.slf4j.MarkerFactory

open class FlexMessageHandler (
    persistenceService: PersistenceService,
    soknadRecordHandler: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService),
    private val brukersvarHandler: BrukersvarHandler = BrukersvarHandler(persistenceService),
    private val behandleSykepengesoeknadHandler: BehandleSykepengesoeknadHandler = BehandleSykepengesoeknadHandler(soknadRecordHandler)
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun handle(flexMessageRecord: FlexMessageRecord) {
        val requestObject = JacksonParser().parse(flexMessageRecord.value)
        log.info(teamLogs,
            "${flexMessageRecord.kilde}: Mottatt melding fra Flex for: ${requestObject.fnr}, status: ${requestObject.status}, type: ${requestObject.type}",
            kv("callId", flexMessageRecord.key),
            kv("kilde", flexMessageRecord.kilde),
            kv("topic", flexMessageRecord.topic),
            kv("partition", flexMessageRecord.partition),
            kv("offset", flexMessageRecord.offset))

        log.info(teamLogs, "mapping fnr to messageID. messageID ${flexMessageRecord.key} is regarding ${requestObject.fnr}",)
        /*
        * SP_1201
        * */
        if  (requestObject.type == SoknadstypeDTO.ARBEIDSTAKERE || requestObject.type == SoknadstypeDTO.GRADERT_REISETILSKUDD){
            log.info("behandler søknad av type ${requestObject.type} ",
                kv("callId",flexMessageRecord.key)
            )
            brukersvarHandler.handleBrukerSporsmaal(flexMessageRecord)
            behandleSykepengesoeknadHandler.handleLovmeRequest(flexMessageRecord)
        }
        else{
            log.info("Melding med id ${flexMessageRecord.key} filtrert ut. Ikke ønsket meldingstype : ${requestObject.type.name}")
        }
    }
}
