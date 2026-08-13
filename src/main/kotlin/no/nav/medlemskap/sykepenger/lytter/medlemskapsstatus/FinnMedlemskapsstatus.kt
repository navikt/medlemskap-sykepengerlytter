package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import no.nav.medlemskap.sykepenger.lytter.domain.Status
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class FinnMedlemskapsstatus(
    private val persistenceService: PersistenceService,
    private val medlemskapsstatusService: MedlemskapsstatusService
) {
    private val logger = FinnMedlemskapsstatusLogger()

    suspend fun finnMedlemskapsstatus(medlemskapsstatusRequest: MedlemskapsstatusRequest, callId: String): Medlemskapsstatus? {
        val vurderingsstatuser = persistenceService.hentVurderingsstatus(medlemskapsstatusRequest.fnr)
        val funnetStatus = vurderingsstatuser.finnMatchendeMedlemskapsperiode(medlemskapsstatusRequest)

        val grunnlag = when (funnetStatus?.status) {
            Status.PAFOLGENDE -> {
                val førstegangssøknadenGrunnlag = vurderingsstatuser.finnGrunnlagForFørstegangssøknaden(funnetStatus)
                    ?: run {
                    logger.logIngenFørstegangssøknad(medlemskapsstatusRequest, callId)
                        return null
                    }

                logger.logKallerSagaMedFørsteVurdering(medlemskapsstatusRequest, førstegangssøknadenGrunnlag, callId)
                medlemskapsstatusRequest.copy(fom = førstegangssøknadenGrunnlag.fom, tom = førstegangssøknadenGrunnlag.tom)
            }
            null -> {
                logger.logIngenMatchendeVurdering(medlemskapsstatusRequest, callId)
                medlemskapsstatusRequest
            }
            else -> medlemskapsstatusRequest
        }

        val medlemskapsstatus = medlemskapsstatusService.hent(grunnlag, callId)
        if (medlemskapsstatus == null) {
            logger.logMedlemskapsstatusIkkeFunnet(grunnlag, callId)
        }
        return medlemskapsstatus
    }
}
