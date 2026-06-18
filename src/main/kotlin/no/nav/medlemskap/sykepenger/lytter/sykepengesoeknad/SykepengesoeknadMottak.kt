package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv

import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.SykepengesoeknadVurdering
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.SykepengesoeknadTilVurdering
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.brukersvar.BrukersvarHandler
import org.slf4j.MarkerFactory

open class SykepengesoeknadMottak (
    persistenceService: PersistenceService,
    sykepengesoeknadVurdering: SykepengesoeknadVurdering = SykepengesoeknadVurdering(Configuration(), persistenceService),
    private val brukersvarHandler: BrukersvarHandler = BrukersvarHandler(persistenceService),
    private val sykepengesoeknadTilVurdering: SykepengesoeknadTilVurdering = SykepengesoeknadTilVurdering(sykepengesoeknadVurdering)
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun handle(sykepengesoeknadRecord: SykepengesoeknadRecord) {
        val requestObject = JacksonParser().parse(sykepengesoeknadRecord.value)
        log.info(teamLogs,
            "${sykepengesoeknadRecord.kilde}: Mottatt melding fra Flex for: ${requestObject.fnr}, status: ${requestObject.status}, type: ${requestObject.type}",
            kv("callId", sykepengesoeknadRecord.key),
            kv("kilde", sykepengesoeknadRecord.kilde),
            kv("topic", sykepengesoeknadRecord.topic),
            kv("partition", sykepengesoeknadRecord.partition),
            kv("offset", sykepengesoeknadRecord.offset))

        log.info(teamLogs, "mapping fnr to messageID. messageID ${sykepengesoeknadRecord.key} is regarding ${requestObject.fnr}",)
        /*
        * SP_1201
        * */
        if  (requestObject.type == SoknadstypeDTO.ARBEIDSTAKERE || requestObject.type == SoknadstypeDTO.GRADERT_REISETILSKUDD){
            log.info("behandler søknad av type ${requestObject.type} ",
                kv("callId",sykepengesoeknadRecord.key)
            )
            brukersvarHandler.handleBrukerSporsmaal(sykepengesoeknadRecord)
            sykepengesoeknadTilVurdering.handleLovmeRequest(sykepengesoeknadRecord)
        }
        else{
            log.info("Melding med id ${sykepengesoeknadRecord.key} filtrert ut. Ikke ønsket meldingstype : ${requestObject.type.name}")
        }
    }
}
