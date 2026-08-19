package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import io.ktor.client.plugins.ResponseException
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Speilvurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.SpeilvurderingRequest

class FinnVurderingForSpeil(
    private val sagaService: SagaService,
    private val opprettNyVurderingForSpeil: OpprettNyVurderingForSpeil
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
                return opprettNyVurderingForSpeil.opprett(speilvurderingRequest, callId)
            }
            logger.feilVedSagaKall(cause)
            throw cause
        }
    }

    suspend fun pingSaga(callId: String): String =
        sagaService.ping(callId)
}