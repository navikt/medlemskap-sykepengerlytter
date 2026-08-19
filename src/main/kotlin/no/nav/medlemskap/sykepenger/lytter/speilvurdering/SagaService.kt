package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaAPI
import no.nav.medlemskap.sykepenger.lytter.config.Configuration

class SagaService(private val sagaApi: SagaAPI) {

    constructor(configuration: Configuration) : this(
        RestClients(
            azureAdClient = AzureAdClient(configuration),
            configuration = configuration
        ).saga(configuration.register.medlemskapSagaBaseUrl)
    )

    suspend fun finnVurdering(request: SpeilvurderingRequest, callId: String): Medlemskapsvurdering =
        sagaApi.finnVurdering(request, callId)

    suspend fun ping(callId: String): String =
        sagaApi.ping(callId)
}
