package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import kotlinx.coroutines.CancellationException
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.service.UtledBrukerinput
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Sykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService

class BehandleSykepengesoeknad(
    private val filtrering: SykepengesoeknadFiltrering,
    private val utledBrukerinput: UtledBrukerinput,
    private val lagreVurderingsstatus: LagreVurderingsstatus,
    private val medlemskapOppslagService: MedlemskapOppslagService
) {
    private val logger = BehandleSykepengesoeknadLogger()

    suspend fun behandle(sykepengesøknad: Sykepengesoeknad) {
        when (val resultat = sykepengesøknad.tilBehandlingsresultat()) {
            is Behandlingsresultat.Duplikat ->
                logger.logDuplikat(resultat.grunnlag)

            is Behandlingsresultat.Påfølgende ->
                logger.logPåfølgende(resultat.grunnlag)

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
            logger.logPassertAlleKriterier(grunnlag)
            val request = lagMedlemskapOppslagRequest(this)
            medlemskapOppslagService.vurderMedlemskap(request, grunnlag.id)
                .also { logger.logSendt(grunnlag) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.logFeiletVurdering(grunnlag, e)
            null
        }
    }

    private fun lagMedlemskapOppslagRequest(sykepengesøknad: Sykepengesoeknad): MedlemskapOppslagRequest {
        val brukerinput = utledBrukerinput.fraSykepengesøknad(sykepengesøknad)
        return MedlemskapOppslagRequestMapper.tilMedlemskapOppslagRequest(sykepengesøknad.sykepengesøknadGrunnlag, brukerinput)
    }
}

private sealed interface Behandlingsresultat {
    data class Duplikat(val grunnlag: SykepengesoeknadGrunnlag) : Behandlingsresultat
    data class Påfølgende(val grunnlag: SykepengesoeknadGrunnlag) : Behandlingsresultat
    data class SkalVurderes(val sykepengesøknad: Sykepengesoeknad) : Behandlingsresultat
}
