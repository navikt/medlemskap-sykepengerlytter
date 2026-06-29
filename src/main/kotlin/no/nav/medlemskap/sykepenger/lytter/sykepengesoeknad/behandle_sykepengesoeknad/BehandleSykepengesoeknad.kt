package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService
import org.slf4j.MarkerFactory

class BehandleSykepengesoeknad(
    private val sykepengesoeknadFiltrering: SykepengesoeknadFiltrering,
    private val utledBrukerinput: UtledBrukerinput,
    private val lagreVurderingsstatus: LagreVurderingsstatus,
    private val medlemskapOppslagService: MedlemskapOppslagService
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun behandle(sykepengeSoknad: LovmeSoknadDTO) {
        when {
            sykepengesoeknadFiltrering.finnDuplikatSomSkalFiltreres(sykepengeSoknad) ->
                sykepengeSoknad.logFunksjoneltLikAnnenSøknad()

            sykepengesoeknadFiltrering.lagreHvisPåfølgendeSøknad(sykepengeSoknad) ->
                sykepengeSoknad.logPåfølgendeSøknad()

            else -> when (val resultat = sendTilRegelmotorForVurdering(sykepengeSoknad)) {
                is VurderingResultat.Ok -> lagreVurderingsstatus.lagreVurderingsstaus(sykepengeSoknad.id, resultat.vurdering)
                VurderingResultat.SkalIkkeLagres -> {
                    // Gradert adresse eller teknisk feil – allerede logget i getVurdering
                }
            }
        }
    }

    private suspend fun sendTilRegelmotorForVurdering(
        sykepengesøknad: LovmeSoknadDTO
    ): VurderingResultat {
        return try {
            sykepengesøknad.logPassertAlleKriterier()
            val request = utledBrukerinput(sykepengesøknad)
            val vurdering = medlemskapOppslagService.vurderMedlemskap(request, sykepengesøknad.id)
            sykepengesøknad.logSendt()
            VurderingResultat.Ok(vurdering)
        } catch (t: Throwable) {
            if (t.message.toString().contains("GradertAdresseException")) {
                log.info("Gradert adresse : key:  ${sykepengesøknad.id}")
            } else {
                sykepengesøknad.logTekniskFeil(t)
            }
            VurderingResultat.SkalIkkeLagres
        }
    }

    private fun utledBrukerinput(sykepengeSoknad: LovmeSoknadDTO): MedlOppslagRequest {
        val brukerinput = utledBrukerinput.utledBrukerinput(sykepengeSoknad)
        return MedlemskapOppslagRequestMapper.map(sykepengeSoknad, brukerinput)
    }

    private fun LovmeSoknadDTO.logPassertAlleKriterier() =
        log.info(
            teamLogs,
            "Søknad med id ${id} har passert alle kriterier og sjekker. Søknaden sendes videre til UtledBrukerinput",
        )

    private fun LovmeSoknadDTO.logFunksjoneltLikAnnenSøknad() =
        log.info(
            teamLogs,
            "soknad med id $id er funksjonelt lik en annen soknad : kryptertFnr : $fnr ",
            kv("callId", id)
        )

    private fun LovmeSoknadDTO.logPåfølgendeSøknad() =
        log.info(
            teamLogs,
            "soknad med id $id er påfølgende en annen søknad. Innslag vil bli laget i db, men ingen vurdering vil bli utført ",
            kv("callId", id)
        )

    private fun LovmeSoknadDTO.logSendt() =
        log.info(
            teamLogs,
            "Søknad videresendt til Lovme - sykmeldingId: $id",
            kv("callId", id)
        )

    private fun LovmeSoknadDTO.logTekniskFeil(t: Throwable) =
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
