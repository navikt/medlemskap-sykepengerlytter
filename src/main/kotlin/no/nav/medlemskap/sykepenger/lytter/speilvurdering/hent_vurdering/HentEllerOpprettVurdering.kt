package no.nav.medlemskap.sykepenger.lytter.speilvurdering.hent_vurdering

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.opprett_vurdering.OpprettNyVurderingForSpeil
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingMapper
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Speilvurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Vurdering

class HentEllerOpprettVurdering(
    private val medlemskapSagaService: MedlemskapSagaService,
    private val opprettNyVurderingForSpeil: OpprettNyVurderingForSpeil,
    private val speilvurderingMapper: SpeilvurderingMapper
) {
    private val logger = HentEllerOpprettVurderingLogger()

    suspend fun finnVurdering(speilvurderingRequest: SpeilvurderingRequest, callId: String): Speilvurdering {
        return when (val vurdering = hentVurdering(speilvurderingRequest, callId)) {
            is Vurdering.VurderingFunnet -> {
                logger.vurderingFunnet(speilvurderingRequest, callId)
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
            Vurdering.VurderingFunnet(medlemskapSagaService.finnVurdering(speilvurderingRequest, callId))
        } catch (cause: ResponseException) {
            if (cause.response.status == HttpStatusCode.NotFound) {
                Vurdering.VurderingIkkeFunnet
            } else {
                logger.feilVedSagaKall(cause)
                throw cause
            }
        }

    suspend fun pingSaga(callId: String): String =
        medlemskapSagaService.ping(callId)
}