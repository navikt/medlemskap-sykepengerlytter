package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
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

    suspend fun behandle(soknadRecord: SoknadRecord) {
        val sykepengeSoknad = soknadRecord.sykepengeSoknad

        when {
            sykepengesoeknadFiltrering.finnDuplikatSomSkalFiltreres(sykepengeSoknad) ->
                sykepengeSoknad.logFunksjoneltLikAnnenSøknad()

            sykepengesoeknadFiltrering.lagreHvisPåfølgendeSøknad(sykepengeSoknad) ->
                sykepengeSoknad.logPåfølgendeSøknad()

            else -> when (val resultat = sendTilRegelmotorForVurdering(soknadRecord)) {
                is VurderingResultat.Ok -> lagreVurderingsstatus.lagreVurderingsstaus(soknadRecord, resultat.vurdering)
                VurderingResultat.SkalIkkeLagres -> {
                    // Gradert adresse eller teknisk feil – allerede logget i getVurdering
                }
            }
        }
    }

    private suspend fun sendTilRegelmotorForVurdering(
        soknadRecord: SoknadRecord
    ): VurderingResultat {
        return try {
            soknadRecord.logPassertAlleKriterier()
            val request = utledBrukerinput(soknadRecord.sykepengeSoknad)
            val vurdering = medlemskapOppslagService.vurderMedlemskap(request, soknadRecord.sykepengeSoknad.id)
            soknadRecord.logSendt()
            VurderingResultat.Ok(vurdering)
        } catch (t: Throwable) {
            if (t.message.toString().contains("GradertAdresseException")) {
                log.info("Gradert adresse : key:  ${soknadRecord.key}, offset: ${soknadRecord.offset}")
            } else {
                soknadRecord.logTekniskFeil(t)
            }
            VurderingResultat.SkalIkkeLagres
        }
    }

    private fun utledBrukerinput(sykepengeSoknad: LovmeSoknadDTO): MedlOppslagRequest {
        val brukerinput = utledBrukerinput.utledBrukerinput(sykepengeSoknad)
        return MedlemskapOppslagRequestMapper.map(sykepengeSoknad, brukerinput)
    }

    private fun SoknadRecord.logPassertAlleKriterier() =
        log.info(
            teamLogs,
            "Søknad med id ${sykepengeSoknad.id} har passert alle kriterier og sjekker. Søknaden sendes videre til UtledBrukerinput",
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

    private fun SoknadRecord.logSendt() =
        log.info(
            teamLogs,
            "Søknad videresendt til Lovme - sykmeldingId: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", sykepengeSoknad.id),
        )

    private fun SoknadRecord.logTekniskFeil(t: Throwable) =
        log.info(
            teamLogs,
            "Teknisk feil ved kall mot LovMe - sykmeldingId: ${sykepengeSoknad.id}, melding:" + t.message,
            kv("callId", sykepengeSoknad.id),
        )

}

private sealed interface VurderingResultat {
    data class Ok(val vurdering: String) : VurderingResultat
    data object SkalIkkeLagres : VurderingResultat
}
