package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import io.ktor.client.plugins.ResponseException
import net.logstash.logback.argument.StructuredArguments
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaAPI
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRequest
import no.nav.medlemskap.sykepenger.lytter.rest.FlexVurderingRespons
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import org.slf4j.MarkerFactory

class HentMedlemskapsstatus(
    private val persistenceService: PersistenceService,
    private val sagaClient: SagaAPI
) {
    constructor(configuration: Configuration, persistenceService: PersistenceService) : this(
        persistenceService,
        RestClients(
            azureAdClient = AzureAdClient(configuration),
            configuration = configuration
        ).saga(configuration.register.medlemskapSagaBaseUrl)
    )

    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun finnFlexVurdering(flexRequest: FlexRequest, callId: String): FlexVurderingRespons? {
        val medlemskap = persistenceService.hentMedlemskap(flexRequest.fnr)
        val found = finnMatchendeMedlemkapsPeriode(medlemskap, flexRequest)

        if (found != null && ErMedlem.PAFOLGENDE != found.medlem) {
            return hentFlexVurdering(flexRequest, found, callId)
        }

        if (found != null && ErMedlem.PAFOLGENDE == found.medlem) {
            val forste = finnRelevantIkkePåfølgende(found, medlemskap)
            if (forste != null) {
                log.info(
                    teamLogs,
                    "kaller saga med første vurdering som ikke er paafolgende : fnr : ${flexRequest.fnr}, fom:${forste.fom}, tom: ${forste.tom}",
                    StructuredArguments.kv("callId", callId)
                )
                return hentFlexVurdering(
                    FlexRequest(flexRequest.sykepengesoknad_id, flexRequest.fnr, forste.fom, forste.tom),
                    forste,
                    callId
                )
            }
            log.info(
                teamLogs,
                "ingen førstegangssøknad funnet for  : ${flexRequest.fnr}, med request fom:${flexRequest.fom}, tom: ${flexRequest.tom}",
                StructuredArguments.kv("callId", callId)
            )
            return null
        }

        log.info(
            teamLogs,
            "ingen matchende treff i vurderinger  funnet for  : ${flexRequest.fnr}, med request fom:${flexRequest.fom}, tom: ${flexRequest.tom}",
            StructuredArguments.kv("callId", callId)
        )
        return hentFlexVurdering(flexRequest, null, callId)
    }

    private suspend fun hentFlexVurdering(
        flexRequest: FlexRequest,
        found: Medlemskap?,
        callId: String
    ): FlexVurderingRespons? {
        return try {
            sagaClient.finnFlexVurdering(flexRequest, callId)
        } catch (cause: ResponseException) {
            if (cause.response.status.value == 404) {
                if (found != null) {
                    log.info(
                        teamLogs,
                        "404 for kall mot saga på : fnr : ${flexRequest.fnr}, fom:${found.fom}, tom: ${found.tom}",
                        StructuredArguments.kv("callId", callId)
                    )
                }
                return null
            }
            log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
            throw cause
        }
    }
}

fun finnRelevantIkkePåfølgende(paafolgende: Medlemskap, medlemskap: List<Medlemskap>): Medlemskap? {
    return medlemskap.sortedByDescending { it.tom }
        .find { it.tom < paafolgende.tom && it.medlem != ErMedlem.PAFOLGENDE }
}

fun finnMatchendeMedlemkapsPeriode(medlemskap: List<Medlemskap>, flexRequest: FlexRequest): Medlemskap? {
    return medlemskap.firstOrNull {
        it.fom.isEqual(flexRequest.fom) &&
            it.tom.isEqual(flexRequest.tom)
    }
}
