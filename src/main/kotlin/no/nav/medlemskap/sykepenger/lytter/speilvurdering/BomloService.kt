package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import io.ktor.client.plugins.ResponseException
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest

class BomloService(
    private val sagaService: SagaService,
    private val medlemskapOppslagService: MedlemskapOppslagService,
    private val medlemskapOppslagMapper: MedlemskapOppslagMapper
) {
    private val logger = BomloServiceLogger()

    suspend fun finnFlexVurdering(bomloRequest: BomloRequest, callId: String): Speilvurdering {
        try {
            val medlemskapsvurdering = sagaService.finnVurdering(bomloRequest, callId)
            logger.vurderingFunnet(callId)
            return SpeilvurderingMapper().fraSaga(medlemskapsvurdering, callId)
        } catch (cause: ResponseException) {
            if (cause.response.status.value == 404) {
                logger.vurderingIkkeFunnet(bomloRequest, callId)
                logger.lovmeKalles(callId, cause)
                val medlemskapOppslagRequest = medlemskapOppslagMapper.map(callId, bomloRequest)
                val medlemskapOppslagVurdering = medlemskapOppslagService.vurderMedlemskapForSpeil(medlemskapOppslagRequest, callId)
                return SpeilvurderingMapper().fraMedlemskapOppslag(medlemskapOppslagVurdering, callId)
            }
            logger.feilVedSagaKall(cause)
            throw cause
        }
    }

    suspend fun pingSaga(callId: String): String =
        sagaService.ping(callId)
}