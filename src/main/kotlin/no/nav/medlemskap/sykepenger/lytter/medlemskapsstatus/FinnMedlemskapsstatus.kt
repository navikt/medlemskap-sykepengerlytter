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
                val førstegangssøknaden = medlemskap.finnFørstegangssøknaden(funnetMedlemskap)
                    ?: run {
                    logger.logIngenFørstegangssøknad(medlemskapsstatusRequest, callId)
                        return null
                    }

                logger.logKallerSagaMedFørsteVurdering(medlemskapsstatusRequest, førstegangssøknaden, callId)
                medlemskapsstatusRequest.copy(fom = førstegangssøknaden.fom, tom = førstegangssøknaden.tom)
            }
            null -> {
                logger.logIngenMatchendeVurdering(medlemskapsstatusRequest, callId)
                medlemskapsstatusRequest
            }
            else -> medlemskapsstatusRequest
        }

        return hentFlexVurdering(grunnlag, callId)
    }

    private suspend fun hentFlexVurdering(
        medlemskapsstatusRequest: MedlemskapsstatusRequest,
        callId: String
    ): Medlemskapsstatus? {
        return try {
            sagaClient.finnFlexVurdering(medlemskapsstatusRequest, callId)
        } catch (cause: ResponseException) {
            if (cause.response.status.value == 404) {
                logger.logSagaVurderingIkkeFunnet(medlemskapsstatusRequest, callId)
                return null
            }
            logger.logHttpFeil(cause.response.status.value, cause)
            throw cause
        }
    }
}

fun List<Medlemskap>.finnFørstegangssøknaden(påfølgende: Medlemskap): Medlemskap? =
    filter { it.tom < påfølgende.tom && it.medlem != Status.PAFOLGENDE }
        .maxByOrNull { it.tom }

fun List<Medlemskap>.finnMatchendeMedlemskapsperiode(
    medlemskapsstatusRequest: MedlemskapsstatusRequest
): Medlemskap? =
    firstOrNull {
        it.fom.isEqual(medlemskapsstatusRequest.fom) &&
            it.tom.isEqual(medlemskapsstatusRequest.tom)
    }
