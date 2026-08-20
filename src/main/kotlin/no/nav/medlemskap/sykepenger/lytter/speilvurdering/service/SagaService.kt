package no.nav.medlemskap.sykepenger.lytter.speilvurdering.service

import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaAPI
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Medlemskapsvurdering

class SagaService(private val sagaApi: SagaAPI) {

    suspend fun finnVurdering(request: SpeilvurderingRequest, callId: String): Medlemskapsvurdering =
        sagaApi.finnVurdering(request, callId)

    suspend fun ping(callId: String): String =
        sagaApi.ping(callId)
}