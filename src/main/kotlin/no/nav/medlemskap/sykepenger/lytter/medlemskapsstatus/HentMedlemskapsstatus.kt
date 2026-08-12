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
        val medlemskapsstatus = persistenceService.hentMedlemskapsstatus(medlemskapsstatusRequest.fnr)
        return when (val oppslag = finnSagaOppslag(medlemskapsstatusRequest, medlemskapsstatus, callId)) {
            FlexVurderingOppslag.IkkeFunnet -> null
            is FlexVurderingOppslag.SkalHentes -> hentFlexVurdering(oppslag.request, callId)
        }
    }

    private suspend fun hentFlexVurdering(
        medlemskapsstatusRequest: MedlemskapsstatusRequest,
        callId: String
    ): Medlemskapsstatus? {
        return try {
            sagaClient.finnFlexVurdering(medlemskapsstatusRequest, callId)
        } catch (cause: ResponseException) {
            if (cause.response.status.value == 404) {
                log.info(
                    teamLogs,
                    "404 for kall mot saga på : fnr : ${medlemskapsstatusRequest.fnr}, " +
                        "fom:${medlemskapsstatusRequest.fom}, tom: ${medlemskapsstatusRequest.tom}",
                    StructuredArguments.kv("callId", callId)
                )
                return null
            }
            log.error("HTTP error i kall mot saga: ${cause.response.status.value} ", cause)
            throw cause
        }
    }

    private fun finnSagaOppslag(
        request: MedlemskapsstatusRequest,
        medlemskap: List<Medlemskap>,
        callId: String
    ): FlexVurderingOppslag {
        return when (val funnet = finnMatchendeMedlemskapsperiode(medlemskap, request)) {
            null -> {
                log.info(
                    teamLogs,
                    "ingen matchende treff i vurderinger funnet for fnr: ${request.fnr}, " +
                        "med request fom: ${request.fom}, tom: ${request.tom}",
                    StructuredArguments.kv("callId", callId)
                )
                FlexVurderingOppslag.SkalHentes(request)
            }

            else -> when (funnet.medlem) {
                ErMedlem.PAFOLGENDE -> finnFørsteVurdering(funnet, medlemskap, request, callId)
                else -> FlexVurderingOppslag.SkalHentes(request)
            }
        }
    }

    private fun finnFørsteVurdering(
        påfølgende: Medlemskap,
        medlemskap: List<Medlemskap>,
        request: MedlemskapsstatusRequest,
        callId: String
    ): FlexVurderingOppslag {
        val første = finnRelevantIkkePåfølgende(påfølgende, medlemskap)
            ?: return run {
                log.info(
                    teamLogs,
                    "ingen førstegangssøknad funnet for fnr: ${request.fnr}, " +
                        "med request fom: ${request.fom}, tom: ${request.tom}",
                    StructuredArguments.kv("callId", callId)
                )
                FlexVurderingOppslag.IkkeFunnet
            }

        log.info(
            teamLogs,
            "kaller saga med første vurdering som ikke er påfølgende for fnr: ${request.fnr}, " +
                "fom: ${første.fom}, tom: ${første.tom}",
            StructuredArguments.kv("callId", callId)
        )
        return FlexVurderingOppslag.SkalHentes(request.copy(fom = første.fom, tom = første.tom))
    }
}

private sealed interface FlexVurderingOppslag {
    data class SkalHentes(val request: MedlemskapsstatusRequest) : FlexVurderingOppslag
    data object IkkeFunnet : FlexVurderingOppslag
}

fun finnRelevantIkkePåfølgende(paafolgende: Medlemskap, medlemskap: List<Medlemskap>): Medlemskap? {
    return medlemskap.sortedByDescending { it.tom }
        .find { it.tom < paafolgende.tom && it.medlem != ErMedlem.PAFOLGENDE }
}

fun finnMatchendeMedlemskapsperiode(
    medlemskap: List<Medlemskap>,
    medlemskapsstatusRequest: MedlemskapsstatusRequest
): Medlemskap? {
    return medlemskap.firstOrNull {
        it.fom.isEqual(medlemskapsstatusRequest.fom) &&
            it.tom.isEqual(medlemskapsstatusRequest.tom)
    }
}
