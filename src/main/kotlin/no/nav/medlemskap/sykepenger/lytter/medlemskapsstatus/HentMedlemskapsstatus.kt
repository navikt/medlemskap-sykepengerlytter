package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import io.ktor.client.plugins.ResponseException
import net.logstash.logback.argument.StructuredArguments
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.clients.saga.SagaAPI
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import org.slf4j.MarkerFactory

class HentMedlemskapsstatus(
    private val persistenceService: PersistenceService,
    private val sagaClient: SagaAPI
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    suspend fun finnFlexVurdering(medlemskapsstatusRequest: MedlemskapsstatusRequest, callId: String): Medlemskapsstatus? {
        val medlemskap = persistenceService.hentMedlemskap(medlemskapsstatusRequest.fnr)
        val found = finnMatchendeMedlemkapsPeriode(medlemskap, medlemskapsstatusRequest)

        if (found != null && ErMedlem.PAFOLGENDE != found.medlem) {
            return hentFlexVurdering(medlemskapsstatusRequest, found, callId)
        }

        if (found != null && ErMedlem.PAFOLGENDE == found.medlem) {
            val forste = finnRelevantIkkePåfølgende(found, medlemskap)
            if (forste != null) {
                log.info(
                    teamLogs,
                    "kaller saga med første vurdering som ikke er paafolgende : fnr : ${medlemskapsstatusRequest.fnr}, fom:${forste.fom}, tom: ${forste.tom}",
                    StructuredArguments.kv("callId", callId)
                )
                return hentFlexVurdering(
                    MedlemskapsstatusRequest(
                        medlemskapsstatusRequest.sykepengesoknad_id,
                        medlemskapsstatusRequest.fnr,
                        forste.fom,
                        forste.tom
                    ),
                    forste,
                    callId
                )
            }
            log.info(
                teamLogs,
                "ingen førstegangssøknad funnet for  : ${medlemskapsstatusRequest.fnr}, med request fom:${medlemskapsstatusRequest.fom}, tom: ${medlemskapsstatusRequest.tom}",
                StructuredArguments.kv("callId", callId)
            )
            return null
        }

        log.info(
            teamLogs,
            "ingen matchende treff i vurderinger  funnet for  : ${medlemskapsstatusRequest.fnr}, med request fom:${medlemskapsstatusRequest.fom}, tom: ${medlemskapsstatusRequest.tom}",
            StructuredArguments.kv("callId", callId)
        )
        return hentFlexVurdering(medlemskapsstatusRequest, null, callId)
    }

    private suspend fun hentFlexVurdering(
        medlemskapsstatusRequest: MedlemskapsstatusRequest,
        found: Medlemskap?,
        callId: String
    ): Medlemskapsstatus? {
        return try {
            sagaClient.finnFlexVurdering(medlemskapsstatusRequest, callId)
        } catch (cause: ResponseException) {
            if (cause.response.status.value == 404) {
                if (found != null) {
                    log.info(
                        teamLogs,
                        "404 for kall mot saga på : fnr : ${medlemskapsstatusRequest.fnr}, fom:${found.fom}, tom: ${found.tom}",
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

fun finnMatchendeMedlemkapsPeriode(medlemskap: List<Medlemskap>, medlemskapsstatusRequest: MedlemskapsstatusRequest): Medlemskap? {
    return medlemskap.firstOrNull {
        it.fom.isEqual(medlemskapsstatusRequest.fom) &&
            it.tom.isEqual(medlemskapsstatusRequest.tom)
    }
}
