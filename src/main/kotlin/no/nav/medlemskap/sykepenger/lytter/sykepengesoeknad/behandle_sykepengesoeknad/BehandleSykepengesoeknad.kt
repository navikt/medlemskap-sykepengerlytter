package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import kotlinx.coroutines.CancellationException
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.service.UtledBrukerinput
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Sykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService
import org.slf4j.MarkerFactory

class BehandleSykepengesoeknad(
    private val filtrering: SykepengesoeknadFiltrering,
    private val utledBrukerinput: UtledBrukerinput,
    private val lagreVurderingsstatus: LagreVurderingsstatus,
    private val medlemskapOppslagService: MedlemskapOppslagService
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun behandle(sykepengesøknad: Sykepengesoeknad) {
        when (val resultat = sykepengesøknad.tilBehandlingsresultat()) {
            is Behandlingsresultat.Duplikat ->
                resultat.grunnlag.logDuplikat()

            is Behandlingsresultat.Påfølgende ->
                resultat.grunnlag.logPåfølgende()

            is Behandlingsresultat.SkalVurderes ->
                vurderOgLagre(resultat.sykepengesøknad)
        }
    }

    private fun Sykepengesoeknad.tilBehandlingsresultat(): Behandlingsresultat {
        val grunnlag = sykepengesøknadGrunnlag

        return when {
            filtrering.erDuplikatOgSvartNeiPåArbeidUtenforNorge(grunnlag) ->
                Behandlingsresultat.Duplikat(grunnlag)

            filtrering.lagreHvisPåfølgendeSøknadOgSvartNeiPåArbeidUtenforNorge(grunnlag) ->
                Behandlingsresultat.Påfølgende(grunnlag)

            else ->
                Behandlingsresultat.SkalVurderes(this)
        }
    }

    private suspend fun vurderOgLagre(sykepengesøknad: Sykepengesoeknad) {
        val vurdering = sykepengesøknad.hentMedlemskapsvurdering() ?: return
        lagreVurderingsstatus.lagreVurderingsstaus(sykepengesøknad.sykepengesøknadGrunnlag.id, vurdering)
    }

    private suspend fun Sykepengesoeknad.hentMedlemskapsvurdering(): String? {
        val grunnlag = sykepengesøknadGrunnlag

        return try {
            grunnlag.logPassertAlleKriterier()
            val request = lagMedlemskapOppslagRequest(this)
            medlemskapOppslagService.vurderMedlemskap(request, grunnlag.id)
                .also { grunnlag.logSendt() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            grunnlag.logFeiletVurdering(e)
            null
        }
    }

    private fun lagMedlemskapOppslagRequest(sykepengesøknad: Sykepengesoeknad): MedlOppslagRequest {
        val brukerinput = utledBrukerinput.fraSykepengesøknad(sykepengesøknad)
        return MedlemskapOppslagRequestMapper.tilMedlemskapOppslagRequest(sykepengesøknad.sykepengesøknadGrunnlag, brukerinput)
    }

    private fun SykepengesoeknadGrunnlag.logPassertAlleKriterier() =
        log.info(
            teamLogs,
            "Søknad med id ${id} har passert alle kriterier og sjekker. Søknaden sendes videre til UtledBrukerinput",
        )

    private fun SykepengesoeknadGrunnlag.logDuplikat() =
        log.info(
            teamLogs,
            "Søknad med id $id er funksjonelt lik en annen soknad : kryptertFnr : $fnr. Sendes ikke videre for vurdering.",
            kv("callId", id)
        )

    private fun SykepengesoeknadGrunnlag.logPåfølgende() =
        log.info(
            teamLogs,
            "Søknad med id $id er påfølgende en annen søknad. Innslag vil bli laget i db, men ingen vurdering vil bli utført ",
            kv("callId", id)
        )

    private fun SykepengesoeknadGrunnlag.logSendt() =
        log.info(
            teamLogs,
            "Søknad videresendt til Lovme - sykmeldingId: $id",
            kv("callId", id)
        )

    private fun SykepengesoeknadGrunnlag.logFeiletVurdering(e: Exception) {
        if (e.erGradertAdresseException()) {
            log.info("Gradert adresse : key:  $id")
        } else {
            logTekniskFeil(e)
        }
    }

    private fun Exception.erGradertAdresseException(): Boolean =
        message?.contains("GradertAdresse") == true

    private fun SykepengesoeknadGrunnlag.logTekniskFeil(e: Exception) =
        log.info(
            teamLogs,
            "Teknisk feil ved kall mot LovMe - sykmeldingId: $id, melding:" + e.message,
            kv("callId", id),
        )

}

private sealed interface Behandlingsresultat {
    data class Duplikat(val grunnlag: SykepengesoeknadGrunnlag) : Behandlingsresultat
    data class Påfølgende(val grunnlag: SykepengesoeknadGrunnlag) : Behandlingsresultat
    data class SkalVurderes(val sykepengesøknad: Sykepengesoeknad) : Behandlingsresultat
}
