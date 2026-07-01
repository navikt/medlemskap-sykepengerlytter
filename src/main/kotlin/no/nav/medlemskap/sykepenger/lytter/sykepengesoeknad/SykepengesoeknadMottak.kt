package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv

import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.BehandleSykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar.BrukersvarMapper
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar.LagreBrukerspoersmaal
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Sykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadMelding
import org.slf4j.MarkerFactory

class SykepengesoeknadMottak(
    private val behandleSykepengesøknad: BehandleSykepengesoeknad,
    private val lagreBrukerspoersmaal: LagreBrukerspoersmaal
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun behandle(sykepengesøknadRecord: SykepengesoeknadMelding) {
        val sykepengesøknadGrunnlag = JacksonParser().lesSykepengesøknadGrunnlag(sykepengesøknadRecord.value)

        if (!harPåkrevdeFelter(sykepengesøknadGrunnlag)) {
            logManglerPåkrevdeFelter(sykepengesøknadRecord)
            return
        }

        logMottattFraFlex(sykepengesøknadRecord, sykepengesøknadGrunnlag)

        val inngangskriterierResultat = Inngangskriterier.vurder(sykepengesøknadGrunnlag)
        if (!inngangskriterierResultat.erOppfylt) {
            logOppfyllerIkkeInngangskriterier(sykepengesøknadRecord, sykepengesøknadGrunnlag, inngangskriterierResultat)
            return
        }

        logOppfyllerInngangskriterier(sykepengesøknadGrunnlag)
        val sykepengesoeknad = Sykepengesoeknad(
            sykepengesoeknadGrunnlag = sykepengesøknadGrunnlag,
            brukersporsmaal = BrukersvarMapper.mapMessage(sykepengesøknadGrunnlag)
        )
        lagreBrukerspoersmaal.lagre(sykepengesoeknad.brukersporsmaal)
        behandleSykepengesøknad.behandle(sykepengesoeknad)
    }

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
        sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag
    ) =
        log.info(
            teamLogs,
            "${sykepengesøknadRecord.kilde}: Mottatt melding fra Flex for: ${sykepengesøknadGrunnlag.fnr}, status: ${sykepengesøknadGrunnlag.status}, type: ${sykepengesøknadGrunnlag.type}",
            kv("callId", sykepengesøknadRecord.key),
            kv("kilde", sykepengesøknadRecord.kilde),
            kv("topic", sykepengesøknadRecord.topic),
            kv("partition", sykepengesøknadRecord.partition),
            kv("offset", sykepengesøknadRecord.offset)
        )

    private fun logOppfyllerIkkeInngangskriterier(
        sykepengesøknadRecord: SykepengesoeknadMelding,
        sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag,
        inngangskriterierResultat: InngangskriterierResultat
    ) =
        log.info(
            teamLogs,
            "Kafka melding med id ${sykepengesøknadRecord.key}, partisjon ${sykepengesøknadRecord.partition} " +
                    "og offset ${sykepengesøknadRecord.offset} filtrert ut. Inngangskriterier ikke oppfylt. " +
                    "Brutte kriterier: ${inngangskriterierResultat.brutteKriterier}. " +
                    "status: ${sykepengesøknadGrunnlag.status}, type: ${sykepengesøknadGrunnlag.type.name}, ettersending: ${sykepengesøknadGrunnlag.ettersending}"
        )

    private fun logOppfyllerInngangskriterier(
        sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag
    ) =
        log.info(
            teamLogs,
            "Sykepengesøknaden oppfyller validering og inngangskriterier. Behandler søknad med id ${sykepengesøknadGrunnlag.id} for person ${sykepengesøknadGrunnlag.fnr}."
        )
}
