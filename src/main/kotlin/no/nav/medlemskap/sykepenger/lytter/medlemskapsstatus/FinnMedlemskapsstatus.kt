package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import io.ktor.client.plugins.ResponseException
import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaAPI
import no.nav.medlemskap.sykepenger.lytter.domain.Status
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class FinnMedlemskapsstatus(
    private val persistenceService: PersistenceService,
    private val sagaClient: SagaAPI
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

        return hentMedlemskapsstatus(grunnlag, callId)
    }

    private suspend fun hentMedlemskapsstatus(
        grunnlag: MedlemskapsstatusRequest,
        callId: String
    ): Medlemskapsstatus? {
        return try {
            sagaClient.hentMedlemskapsstatus(grunnlag, callId)
        } catch (cause: ResponseException) {
            if (cause.response.status.value == 404) {
                logger.logMedlemskapsstatusIkkeFunnet(grunnlag, callId)
                return null
            }
            logger.logHttpFeil(cause.response.status.value, cause)
            throw cause
        }
    }
}

fun List<Medlemskap>.finnGrunnlagForFørstegangssøknaden(påfølgende: Medlemskap): Medlemskap? =
    filter { it.tom < påfølgende.tom && it.medlem != Status.PAFOLGENDE }
        .maxByOrNull { it.tom }

fun List<Medlemskap>.finnMatchendeMedlemskapsperiode(
    medlemskapsstatusRequest: MedlemskapsstatusRequest
): Medlemskap? =
    firstOrNull {
        it.fom.isEqual(medlemskapsstatusRequest.fom) &&
            it.tom.isEqual(medlemskapsstatusRequest.tom)
    }
