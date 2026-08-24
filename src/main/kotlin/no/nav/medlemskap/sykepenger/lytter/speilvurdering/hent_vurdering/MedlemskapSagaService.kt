package no.nav.medlemskap.sykepenger.lytter.speilvurdering.hent_vurdering

import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_saga.MedlemskapSagaAPI
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Medlemskapsvurdering

class MedlemskapSagaService(private val medlemskapSagaApi: MedlemskapSagaAPI) {

    suspend fun finnVurdering(request: SpeilvurderingRequest, callId: String): Medlemskapsvurdering =
        medlemskapSagaApi.finnVurdering(request, callId)
            .let(objectMapper::readTree)
            .let { response ->
                Medlemskapsvurdering(
                    if (response.isTextual) {
                        objectMapper.readTree(response.asText())
                    } else {
                        response
                    }
                )
            }

    suspend fun ping(callId: String): String =
        medlemskapSagaApi.ping(callId)
}