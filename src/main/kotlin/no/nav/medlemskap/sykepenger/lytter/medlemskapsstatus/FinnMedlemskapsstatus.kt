package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import no.nav.medlemskap.sykepenger.lytter.domain.Status
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class FinnMedlemskapsstatus(
    private val persistenceService: PersistenceService,
    private val medlemskapSagaVurderingService: MedlemskapSagaVurderingService
) {
    private val logger = FinnMedlemskapsstatusLogger()

    suspend fun finnMedlemskapsstatus(medlemskapsstatusRequest: MedlemskapsstatusRequest, callId: String): Vurdering? {
        val klassifiseringer = persistenceService.hentKlassifisering(medlemskapsstatusRequest.fnr)
        val funnetStatus = klassifiseringer.finnKlassifiseringForPeriode(medlemskapsstatusRequest.fom, medlemskapsstatusRequest.tom)

        val grunnlag = when (funnetStatus?.status) {
            Status.PAFOLGENDE -> {
                val førstegangssøknadenGrunnlag = klassifiseringer.finnGrunnlagForFørstegangssøknaden(funnetStatus)
                    ?: run {
                    logger.logIngenFørstegangssøknadFunnet(medlemskapsstatusRequest, callId)
                        return null
                    }

                logger.logHenterMedlemskapsstatusForFørstegangssøknad(medlemskapsstatusRequest, førstegangssøknadenGrunnlag, callId)

                medlemskapsstatusRequest.copy(fom = førstegangssøknadenGrunnlag.fom, tom = førstegangssøknadenGrunnlag.tom)
            }
            null -> {
                logger.logFantIngenVurderingsstatus(medlemskapsstatusRequest, callId)
                medlemskapsstatusRequest
            }
            else -> medlemskapsstatusRequest
        }

        val vurdering = medlemskapSagaVurderingService.hent(grunnlag, callId)
        if (vurdering == null) {
            logger.logMedlemskapsstatusIkkeFunnet(grunnlag, callId)
        }
        return vurdering
    }
}
