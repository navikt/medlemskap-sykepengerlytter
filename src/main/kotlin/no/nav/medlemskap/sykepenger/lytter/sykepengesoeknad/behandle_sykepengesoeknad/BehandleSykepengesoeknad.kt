package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
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

    suspend fun behandle(sykepengesoeknad: Sykepengesoeknad) {
        when (val resultat = sykepengesoeknad.tilBehandlingsresultat()) {
            is Behandlingsresultat.Duplikat ->
                resultat.grunnlag.logDuplikat()

            is Behandlingsresultat.Påfølgende ->
                resultat.grunnlag.logPåfølgende()

            is Behandlingsresultat.SkalVurderes ->
                vurderOgLagre(resultat.grunnlag)
        }
    }

    private fun Sykepengesoeknad.tilBehandlingsresultat(): Behandlingsresultat {
        val grunnlag = sykepengesoeknadGrunnlag

        return when {
            filtrering.erDuplikatOgSvartNeiPåArbeidUtenforNorge(grunnlag) ->
                Behandlingsresultat.Duplikat(grunnlag)

            filtrering.lagreHvisPåfølgendeSøknadOgSvartNeiPåArbeidUtenforNorge(grunnlag) ->
                Behandlingsresultat.Påfølgende(grunnlag)

            else ->
                Behandlingsresultat.SkalVurderes(grunnlag)
        }
    }

    private suspend fun vurderOgLagre(grunnlag: SykepengesoeknadGrunnlag) {
        when (val resultat = grunnlag.vurderMedlemskap()) {
            is VurderingResultat.VurderingSkalLagres ->
                lagreVurderingsstatus.lagreVurderingsstaus(grunnlag.id, resultat.vurdering)

            VurderingResultat.VurderingSkalIkkeLagres -> Unit
        }
    }

    private suspend fun SykepengesoeknadGrunnlag.vurderMedlemskap(): VurderingResultat {
        return try {
            logPassertAlleKriterier()
            val request = utledBrukerinput(this)
            val vurdering = medlemskapOppslagService.vurderMedlemskap(request, id)
            logSendt()
            VurderingResultat.VurderingSkalLagres(vurdering)
        } catch (t: Throwable) {
            if (t.message.toString().contains("GradertAdresseException")) {
                log.info("Gradert adresse : key:  $id")
            } else {
                logTekniskFeil(t)
            }
            VurderingResultat.VurderingSkalIkkeLagres
        }
    }

    private fun utledBrukerinput(sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag): MedlOppslagRequest {
        val brukerinput = utledBrukerinput.utledBrukerinput(sykepengesøknadGrunnlag)
        return MedlemskapOppslagRequestMapper.map(sykepengesøknadGrunnlag, brukerinput)
    }

    private fun SykepengesoeknadGrunnlag.logPassertAlleKriterier() =
        log.info(
            teamLogs,
            "Søknad med id ${id} har passert alle kriterier og sjekker. Søknaden sendes videre til UtledBrukerinput",
        )

    private fun SykepengesoeknadGrunnlag.logDuplikat() =
        log.info(
            teamLogs,
            "soknad med id $id er funksjonelt lik en annen soknad : kryptertFnr : $fnr ",
            kv("callId", id)
        )

    private fun SykepengesoeknadGrunnlag.logPåfølgende() =
        log.info(
            teamLogs,
            "soknad med id $id er påfølgende en annen søknad. Innslag vil bli laget i db, men ingen vurdering vil bli utført ",
            kv("callId", id)
        )

    private fun SykepengesoeknadGrunnlag.logSendt() =
        log.info(
            teamLogs,
            "Søknad videresendt til Lovme - sykmeldingId: $id",
            kv("callId", id)
        )

    private fun SykepengesoeknadGrunnlag.logTekniskFeil(t: Throwable) =
        log.info(
            teamLogs,
            "Teknisk feil ved kall mot LovMe - sykmeldingId: $id, melding:" + t.message,
            kv("callId", id),
        )

}

private sealed interface Behandlingsresultat {
    data class Duplikat(val grunnlag: SykepengesoeknadGrunnlag) : Behandlingsresultat
    data class Påfølgende(val grunnlag: SykepengesoeknadGrunnlag) : Behandlingsresultat
    data class SkalVurderes(val grunnlag: SykepengesoeknadGrunnlag) : Behandlingsresultat
}

private sealed interface VurderingResultat {
    data class VurderingSkalLagres(val vurdering: String) : VurderingResultat
    data object VurderingSkalIkkeLagres : VurderingResultat
}
