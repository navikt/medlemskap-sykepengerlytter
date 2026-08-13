package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import no.nav.medlemskap.sykepenger.lytter.domain.Status
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class FinnMedlemskapsstatus(
    private val persistenceService: PersistenceService,
    private val medlemskapsstatusService: MedlemskapsstatusService
) {
    private val logger = FinnMedlemskapsstatusLogger()

    suspend fun finnMedlemskapsstatus(medlemskapsstatusRequest: MedlemskapsstatusRequest, callId: String): Medlemskapsstatus? {
        val medlemskap = persistenceService.hentMedlemskap(medlemskapsstatusRequest.fnr)
        val funnetMedlemskap = medlemskap.finnMatchendeMedlemskapsperiode(medlemskapsstatusRequest)

        val grunnlag = when (funnetMedlemskap?.medlem) {
            Status.PAFOLGENDE -> {
                val førstegangssøknadenGrunnlag = medlemskap.finnGrunnlagForFørstegangssøknaden(funnetMedlemskap)
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
