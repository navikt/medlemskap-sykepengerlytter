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

    suspend fun behandle(melding: SykepengesoeknadMelding) {
        when (val resultat = melding.tilMottakResultat()) {
            is MottakResultat.ManglerPåkrevdeFelter ->
                logManglerPåkrevdeFelter(resultat.melding)

            is MottakResultat.OppfyllerIkkeInngangskriterier ->
                logOppfyllerIkkeInngangskriterier(
                    resultat.melding,
                    resultat.grunnlag,
                    resultat.inngangskriterierResultat
                )

            is MottakResultat.SkalBehandles ->
                behandle(resultat.sykepengesøknad)
        }
    }

    private suspend fun behandle(sykepengesøknad: Sykepengesoeknad) {
        logOppfyllerInngangskriterier(sykepengesøknad.sykepengesoeknadGrunnlag)
        lagreBrukerspoersmaal.lagre(sykepengesøknad.brukersporsmaal)
        behandleSykepengesøknad.behandle(sykepengesøknad)
    }

    private fun SykepengesoeknadMelding.tilMottakResultat(): MottakResultat {
        val grunnlag = tilSykepengesøknadGrunnlag()

        if (!harPåkrevdeFelter(grunnlag)) {
            return MottakResultat.ManglerPåkrevdeFelter(this)
        }

        logMottattFraFlex(this, grunnlag)

        val inngangskriterierResultat = Inngangskriterier.vurder(grunnlag)
        if (!inngangskriterierResultat.erOppfylt) {
            return MottakResultat.OppfyllerIkkeInngangskriterier(this, grunnlag, inngangskriterierResultat)
        }

        return MottakResultat.SkalBehandles(grunnlag.tilSykepengesøknad())
    }

    private fun SykepengesoeknadMelding.tilSykepengesøknadGrunnlag(): SykepengesoeknadGrunnlag =
        JacksonParser().lesSykepengesøknadGrunnlag(value)

    private fun SykepengesoeknadGrunnlag.tilSykepengesøknad(): Sykepengesoeknad =
        Sykepengesoeknad(
            sykepengesoeknadGrunnlag = this,
            brukersporsmaal = BrukersvarMapper.mapBrukerspørsmål(this)
        )

    private sealed interface MottakResultat {
        data class ManglerPåkrevdeFelter(
            val melding: SykepengesoeknadMelding
        ) : MottakResultat

        data class OppfyllerIkkeInngangskriterier(
            val melding: SykepengesoeknadMelding,
            val grunnlag: SykepengesoeknadGrunnlag,
            val inngangskriterierResultat: InngangskriterierResultat
        ) : MottakResultat

        data class SkalBehandles(
            val sykepengesøknad: Sykepengesoeknad
        ) : MottakResultat
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
