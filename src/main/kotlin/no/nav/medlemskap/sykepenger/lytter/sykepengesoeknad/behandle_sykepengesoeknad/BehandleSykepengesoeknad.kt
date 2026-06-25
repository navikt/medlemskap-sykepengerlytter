package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService

class BehandleSykepengesoeknad(
    private val sykepengesoeknadFiltrering: SykepengesoeknadFiltrering,
    private val utledBrukerinput: UtledBrukerinput,
    private val lagreVurderingsstatus: LagreVurderingsstatus,
    private val medlemskapOppslagService: MedlemskapOppslagService
) {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    suspend fun behandle(soknadRecord: SoknadRecord) {
        val sykepengeSoknad = soknadRecord.sykepengeSoknad

        when {
            sykepengesoeknadFiltrering.finnDuplikatSomSkalFiltreres(sykepengeSoknad) ->
                sykepengeSoknad.logFunksjoneltLikAnnenSøknad()

            sykepengesoeknadFiltrering.lagreHvisPåfølgendeSøknad(sykepengeSoknad) ->
                sykepengeSoknad.logPåfølgendeSøknad()

            else -> when (val resultat = getVurdering(soknadRecord)) {
                is VurderingResultat.Ok -> lagreVurderingsstatus.lagreVurderingsstaus(soknadRecord, resultat.vurdering)
                VurderingResultat.SkalIkkeLagres -> {
                    // Gradert adresse eller teknisk feil – allerede logget i getVurdering
                }
            }
        }
    }

    private suspend fun getVurdering(
        soknadRecord: SoknadRecord
    ): VurderingResultat {
        return try {
            val vurdering = utledBrukersvarOgKjørRegelmotor(soknadRecord.sykepengeSoknad)
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

    private suspend fun utledBrukersvarOgKjørRegelmotor(sykepengeSoknad: LovmeSoknadDTO): String {
        val brukerinput = utledBrukerinput.utledBrukerinput(sykepengeSoknad)
        val medlemskapOppslagRequest = MedlemskapOppslagRequestMapper.map(sykepengeSoknad, brukerinput)
        return medlemskapOppslagService.vurderMedlemskap(medlemskapOppslagRequest, brukerinput.søknadsParametere.callId)
    }

    private fun LovmeSoknadDTO.logFunksjoneltLikAnnenSøknad() =
        log.info(
            "soknad med id $id er funksjonelt lik en annen soknad : kryptertFnr : $fnr ",
            kv("callId", id)
        )

    private fun LovmeSoknadDTO.logPåfølgendeSøknad() =
        log.info(
            "soknad med id $id er påfølgende en annen søknad. Innslag vil bli laget i db, men ingen vurdering vil bli utført ",
            kv("callId", id)
        )

    private fun SoknadRecord.logSendt() =
        log.info(
            "Søknad videresendt til Lovme - sykmeldingId: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", sykepengeSoknad.id),
        )

    private fun SoknadRecord.logTekniskFeil(t: Throwable) =
        log.info(
            "Teknisk feil ved kall mot LovMe - sykmeldingId: ${sykepengeSoknad.id}, melding:" + t.message,
            kv("callId", sykepengeSoknad.id),
        )

}

private sealed interface VurderingResultat {
    data class Ok(val vurdering: String) : VurderingResultat
    data object SkalIkkeLagres : VurderingResultat
}
