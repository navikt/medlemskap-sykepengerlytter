package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_saga.MedlemskapSagaAPI

class MedlemskapSagaVurderingService(
    private val sagaClient: MedlemskapSagaAPI
) {
    suspend fun hent(
        grunnlag: VurderingRequest,
        callId: String
    ): Vurdering? =
        try {
            sagaClient.hentVurdering(grunnlag, callId)
        } catch (cause: ResponseException) {
            when (cause.response.status) {
                HttpStatusCode.NotFound -> null
                else -> throw cause
            }
        }
}
