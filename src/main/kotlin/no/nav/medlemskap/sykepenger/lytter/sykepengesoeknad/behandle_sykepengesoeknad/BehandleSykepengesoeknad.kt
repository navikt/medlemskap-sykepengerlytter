package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
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

    suspend fun behandle(sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag) {
        when {
            filtrering.erDuplikatOgSvartNeiPåArbeidUtenforNorge(sykepengesøknadGrunnlag) ->
                sykepengesøknadGrunnlag.logDuplikat()

            filtrering.lagreHvisPåfølgendeSøknadOgSvartNeiPåArbeidUtenforNorge(sykepengesøknadGrunnlag) ->
                sykepengesøknadGrunnlag.logPåfølgende()

            else -> when (val resultat = sendTilRegelmotorForVurdering(sykepengesøknadGrunnlag)) {
                is VurderingResultat.Ok -> lagreVurderingsstatus.lagreVurderingsstaus(sykepengesøknadGrunnlag.id, resultat.vurdering)
                VurderingResultat.SkalIkkeLagres -> {
                    // Gradert adresse eller teknisk feil – allerede logget i getVurdering
                }
            }
        }
    }

    private suspend fun sendTilRegelmotorForVurdering(
        sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag
    ): VurderingResultat {
        return try {
            sykepengesøknadGrunnlag.logPassertAlleKriterier()
            val request = utledBrukerinput(sykepengesøknadGrunnlag)
            val vurdering = medlemskapOppslagService.vurderMedlemskap(request, sykepengesøknadGrunnlag.id)
            sykepengesøknadGrunnlag.logSendt()
            VurderingResultat.Ok(vurdering)
        } catch (t: Throwable) {
            if (t.message.toString().contains("GradertAdresseException")) {
                log.info("Gradert adresse : key:  ${sykepengesøknadGrunnlag.id}")
            } else {
                sykepengesøknadGrunnlag.logTekniskFeil(t)
            }
            VurderingResultat.SkalIkkeLagres
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

private sealed interface VurderingResultat {
    data class Ok(val vurdering: String) : VurderingResultat
    data object SkalIkkeLagres : VurderingResultat
}
