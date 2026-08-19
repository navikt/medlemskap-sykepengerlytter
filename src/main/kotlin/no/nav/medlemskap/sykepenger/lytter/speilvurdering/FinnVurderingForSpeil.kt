package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import io.ktor.client.plugins.ResponseException

class FinnVurderingForSpeil(
    private val sagaService: SagaService,
    private val medlemskapOppslagService: MedlemskapOppslagService,
    private val medlemskapOppslagMapper: MedlemskapOppslagMapper
) {
    private val logger = FinnVurderingForSpeilLogger()
    private val speilvurderingMapper = SpeilvurderingMapper()

    suspend fun finnVurdering(speilvurderingRequest: SpeilvurderingRequest, callId: String): Speilvurdering {
        try {
            val medlemskapsvurdering = sagaService.finnVurdering(speilvurderingRequest, callId)
            logger.vurderingFunnet(callId)
            return speilvurderingMapper.fraSaga(medlemskapsvurdering, callId)
        } catch (cause: ResponseException) {
            if (cause.response.status.value == 404) {
                logger.vurderingIkkeFunnet(speilvurderingRequest, callId)
                logger.lovmeKalles(callId, cause)
                val medlemskapOppslagRequest = medlemskapOppslagMapper.map(callId, speilvurderingRequest)
                val medlemskapOppslagVurdering = medlemskapOppslagService.vurderMedlemskapForSpeil(medlemskapOppslagRequest, callId)
                return speilvurderingMapper.fraMedlemskapOppslag(medlemskapOppslagVurdering, callId)
            }
            logger.feilVedSagaKall(cause)
            throw cause
        }
    }

    suspend fun pingSaga(callId: String): String =
        sagaService.ping(callId)
}