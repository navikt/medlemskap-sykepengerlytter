package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.jackson.MedlemskapVurdertParser
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class LagreVurderingsstatus(
    private val persistenceService: PersistenceService
) {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    fun lagreVurderingsstaus(callId: String, vurdering: String) {
        try {
            persistenceService.lagreLovmeResponse(callId, MedlemskapVurdertParser().parse(vurdering))
        } catch (t: Exception) {
            log.error(
                "Teknisk feil ved lagring av LovmeRespons i databasen, - sykmeldingId: $callId . melding : ${t.message}",
                kv("callId", callId)
            )
        }
    }
}
