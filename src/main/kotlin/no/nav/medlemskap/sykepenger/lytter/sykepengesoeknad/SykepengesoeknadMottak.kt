package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv

import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.SykepengesoeknadVurdering
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.BehandleSykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.brukersvar.BehandleBrukersvar
import org.slf4j.MarkerFactory

class SykepengesoeknadMottak (
    persistenceService: PersistenceService,
    sykepengesoeknadVurdering: SykepengesoeknadVurdering = SykepengesoeknadVurdering(Configuration(), persistenceService),
    private val behandleBrukersvar: BehandleBrukersvar = BehandleBrukersvar(persistenceService),
    private val behandleSykepengesoeknad: BehandleSykepengesoeknad = BehandleSykepengesoeknad(sykepengesoeknadVurdering)
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun behandle(sykepengesøknadRecord: SykepengesoeknadRecord) {
        val sykepengesøknad = JacksonParser().parse(sykepengesøknadRecord.value)

        logMottattFraFlex(sykepengesøknadRecord, sykepengesøknad)
        logFnrTilMeldingId(sykepengesøknadRecord, sykepengesøknad)

        if (!Mottakskriterier.erOppfylt(sykepengesøknad)) {
            logFiltrertUtPåSøknadstype(sykepengesøknadRecord, sykepengesøknad)
            return
        }

        logSkalBehandles(sykepengesøknadRecord, sykepengesøknad)
        behandleBrukersvar.behandleBrukerspørsmål(sykepengesøknadRecord)
        behandleSykepengesoeknad.behandleSykepengesøknad(sykepengesøknadRecord)
    }

    private fun logMottattFraFlex(
        sykepengesøknadRecord: SykepengesoeknadRecord,
        sykepengesøknad: LovmeSoknadDTO
    ) =
        log.info(
            teamLogs,
            "${sykepengesøknadRecord.kilde}: Mottatt melding fra Flex for: ${sykepengesøknad.fnr}, status: ${sykepengesøknad.status}, type: ${sykepengesøknad.type}",
            kv("callId", sykepengesøknadRecord.key),
            kv("kilde", sykepengesøknadRecord.kilde),
            kv("topic", sykepengesøknadRecord.topic),
            kv("partition", sykepengesøknadRecord.partition),
            kv("offset", sykepengesøknadRecord.offset)
        )

    private fun logFnrTilMeldingId(
        sykepengesøknadRecord: SykepengesoeknadRecord,
        sykepengesøknad: LovmeSoknadDTO
    ) =
        log.info(
            teamLogs,
            "mapping fnr to messageID. messageID ${sykepengesøknadRecord.key} is regarding ${sykepengesøknad.fnr}",
        )

    private fun logFiltrertUtPåSøknadstype(
        sykepengesøknadRecord: SykepengesoeknadRecord,
        sykepengesøknad: LovmeSoknadDTO
    ) =
        log.info(
            "Melding med id ${sykepengesøknadRecord.key} filtrert ut. Ikke ønsket meldingstype : ${sykepengesøknad.type.name}"
        )

    private fun logSkalBehandles(
        sykepengesøknadRecord: SykepengesoeknadRecord,
        sykepengesøknad: LovmeSoknadDTO
    ) =
        log.info(
            "behandler søknad av type ${sykepengesøknad.type} ",
            kv("callId", sykepengesøknadRecord.key)
        )
}
