package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Speilvurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.SpeilvurderingRequest
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Vurdering

class FinnVurderingForSpeil(
    private val sagaService: SagaService,
    private val opprettNyVurderingForSpeil: OpprettNyVurderingForSpeil
) {
    private val logger = FinnVurderingForSpeilLogger()
    private val speilvurderingMapper = SpeilvurderingMapper()

    suspend fun finnVurdering(speilvurderingRequest: SpeilvurderingRequest, callId: String): Speilvurdering {
        return when (val vurdering = hentVurdering(speilvurderingRequest, callId)) {
            is Vurdering.VurderingFunnet -> {
                logger.vurderingFunnet(callId)
                speilvurderingMapper.fraSaga(vurdering.vurdering, callId)
            }

            Vurdering.VurderingIkkeFunnet -> {
                logger.vurderingIkkeFunnet(speilvurderingRequest, callId)
                opprettNyVurderingForSpeil.opprett(speilvurderingRequest, callId)
            }
        }
    }

    private suspend fun hentVurdering(
        speilvurderingRequest: SpeilvurderingRequest,
        callId: String
    ): Vurdering =
        try {
            Vurdering.VurderingFunnet(sagaService.finnVurdering(speilvurderingRequest, callId))
        } catch (cause: ResponseException) {
            if (cause.response.status == HttpStatusCode.NotFound) {
                Vurdering.VurderingIkkeFunnet
            } else {
                logger.feilVedSagaKall(cause)
                throw cause
            }
        }

    suspend fun pingSaga(callId: String): String =
        sagaService.ping(callId)
}