package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv

import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.BehandleSykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar.BehandleBrukersvar
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadMelding
import org.slf4j.MarkerFactory

class SykepengesoeknadMottak(
    private val behandleSykepengesøknad: BehandleSykepengesoeknad,
    private val behandleBrukersvar: BehandleBrukersvar
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun behandle(sykepengesøknadRecord: SykepengesoeknadMelding) {
        val sykepengesøknad = JacksonParser().parse(sykepengesøknadRecord.value)

        if (!harPåkrevdeFelter(sykepengesøknad)) {
            logManglerPåkrevdeFelter(sykepengesøknadRecord)
            return
        }

        logMottattFraFlex(sykepengesøknadRecord, sykepengesøknad)

        val inngangskriterierResultat = Inngangskriterier.vurder(sykepengesøknad)
        if (!inngangskriterierResultat.erOppfylt) {
            logOppfyllerIkkeInngangskriterier(sykepengesøknadRecord, sykepengesøknad, inngangskriterierResultat)
            return
        }

        logOppfyllerInngangskriterier(sykepengesøknadRecord, sykepengesøknad)
        behandleBrukersvar.behandle(sykepengesøknadRecord)
        behandleSykepengesøknad.behandle(sykepengesøknad)
    }

    private fun harPåkrevdeFelter(sykepengesøknad: LovmeSoknadDTO): Boolean =
        sykepengesøknad.fnr.isNotBlank() && sykepengesøknad.id.isNotBlank()

    private fun logManglerPåkrevdeFelter(
        sykepengesøknadRecord: SykepengesoeknadMelding
    ) =
        log.info(
            teamLogs,
            "Kafka melding med id ${sykepengesøknadRecord.key}, partisjon ${sykepengesøknadRecord.partition} " +
                    "og offset ${sykepengesøknadRecord.offset} filtrert ut. Mangler påkrevde felter for fnr og id i meldingen."
        )

    private fun logMottattFraFlex(
        sykepengesøknadRecord: SykepengesoeknadMelding,
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

    private fun logOppfyllerIkkeInngangskriterier(
        sykepengesøknadRecord: SykepengesoeknadMelding,
        sykepengesøknad: LovmeSoknadDTO,
        inngangskriterierResultat: InngangskriterierResultat
    ) =
        log.info(
            teamLogs,
            "Kafka melding med id ${sykepengesøknadRecord.key}, partisjon ${sykepengesøknadRecord.partition} " +
                    "og offset ${sykepengesøknadRecord.offset} filtrert ut. Inngangskriterier ikke oppfylt. " +
                    "Brutte kriterier: ${inngangskriterierResultat.brutteKriterier}. " +
                    "status: ${sykepengesøknad.status}, type: ${sykepengesøknad.type.name}, ettersending: ${sykepengesøknad.ettersending}"
        )

    private fun logOppfyllerInngangskriterier(
        sykepengesøknadRecord: SykepengesoeknadMelding,
        sykepengesøknad: LovmeSoknadDTO
    ) =
        log.info(
            teamLogs,
            "Sykepengesøknaden oppfyller validering og inngangskriterier. Behandler søknad med id ${sykepengesøknadRecord.key} for person ${sykepengesøknad.fnr}."
        )
}
