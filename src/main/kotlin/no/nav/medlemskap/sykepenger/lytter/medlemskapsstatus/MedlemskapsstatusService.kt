package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaAPI

class MedlemskapsstatusService(
    private val sagaClient: SagaAPI
) {
    suspend fun hent(
        grunnlag: MedlemskapsstatusRequest,
        callId: String
    ): Medlemskapsstatus? =
        try {
            sagaClient.hentMedlemskapsstatus(grunnlag, callId)
        } catch (cause: ResponseException) {
            when (cause.response.status) {
                HttpStatusCode.NotFound -> null
                else -> throw cause
            }
        }
}
