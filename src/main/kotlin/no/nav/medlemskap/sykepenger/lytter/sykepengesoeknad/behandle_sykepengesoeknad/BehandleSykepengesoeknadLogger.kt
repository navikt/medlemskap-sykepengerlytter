package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import org.slf4j.MarkerFactory

internal class BehandleSykepengesoeknadLogger {
    private companion object {
        val log = KotlinLogging.logger { }
        val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    fun logPassertAlleKriterier(grunnlag: SykepengesoeknadGrunnlag) =
        log.info(
            teamLogs,
            "Søknad med id ${grunnlag.id} har passert alle kriterier og sjekker. Søknaden sendes videre til UtledBrukerinput",
        )

    fun logDuplikat(grunnlag: SykepengesoeknadGrunnlag) =
        log.info(
            teamLogs,
            "Søknad med id ${grunnlag.id} er funksjonelt lik en annen soknad : kryptertFnr : ${grunnlag.fnr}. Sendes ikke videre for vurdering.",
            kv("callId", grunnlag.id)
        )

    fun logPåfølgende(grunnlag: SykepengesoeknadGrunnlag) =
        log.info(
            teamLogs,
            "Søknad med id ${grunnlag.id} er påfølgende en annen søknad. Innslag vil bli laget i db, men ingen vurdering vil bli utført ",
            kv("callId", grunnlag.id)
        )

    fun logSendt(grunnlag: SykepengesoeknadGrunnlag) =
        log.info(
            teamLogs,
            "Søknad videresendt til Lovme - sykmeldingId: ${grunnlag.id}",
            kv("callId", grunnlag.id)
        )

    fun logFeiletVurdering(grunnlag: SykepengesoeknadGrunnlag, e: Exception) {
        if (e.erGradertAdresseException()) {
            log.info("Gradert adresse : key:  ${grunnlag.id}")
        } else {
            logTekniskFeil(grunnlag, e)
        }
    }

    private fun Exception.erGradertAdresseException(): Boolean =
        message?.contains("GradertAdresse") == true

    private fun logTekniskFeil(grunnlag: SykepengesoeknadGrunnlag, e: Exception) =
        log.info(
            teamLogs,
            "Teknisk feil ved kall mot LovMe - sykmeldingId: ${grunnlag.id}, melding:" + e.message,
            kv("callId", grunnlag.id),
        )
}
