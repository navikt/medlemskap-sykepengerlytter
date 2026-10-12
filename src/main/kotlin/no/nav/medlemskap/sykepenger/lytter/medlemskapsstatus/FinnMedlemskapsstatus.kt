package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import no.nav.medlemskap.sykepenger.lytter.domain.Status
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class FinnMedlemskapsstatus(
    private val persistenceService: PersistenceService,
    private val medlemskapSagaVurderingService: MedlemskapSagaVurderingService
) {
    private val logger = FinnMedlemskapsstatusLogger()

    suspend fun finnMedlemskapsstatus(vurderingRequest: VurderingRequest, callId: String): Vurdering? {
        val klassifiseringer = persistenceService.hentKlassifisering(vurderingRequest.fnr)
        val funnetStatus = klassifiseringer.finnKlassifiseringForPeriode(vurderingRequest.fom, vurderingRequest.tom)

        val grunnlag = when (funnetStatus?.status) {
            Status.PAFOLGENDE -> {
                val førstegangssøknadenGrunnlag = klassifiseringer.finnGrunnlagForFørstegangssøknaden(funnetStatus)
                    ?: run {
                    logger.logIngenFørstegangssøknadFunnet(vurderingRequest, callId)
                        return null
                    }

                logger.logHenterMedlemskapsstatusForFørstegangssøknad(vurderingRequest, førstegangssøknadenGrunnlag, callId)

                vurderingRequest.copy(fom = førstegangssøknadenGrunnlag.fom, tom = førstegangssøknadenGrunnlag.tom)
            }
            null -> {
                logger.logFantIngenVurderingsstatus(vurderingRequest, callId)
                vurderingRequest
            }
            else -> vurderingRequest
        }

        val vurdering = medlemskapSagaVurderingService.hent(grunnlag, callId)
        if (vurdering == null) {
            logger.logMedlemskapsstatusIkkeFunnet(grunnlag, callId)
        }
        return vurdering
    }
}
