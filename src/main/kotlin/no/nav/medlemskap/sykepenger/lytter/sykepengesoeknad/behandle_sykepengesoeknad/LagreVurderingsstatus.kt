package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.jackson.MedlemskapVurdertParser
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class LagreVurderingsstatus(
    private val persistenceService: PersistenceService
) {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    fun lagreVurderingsstaus(soknadRecord: SoknadRecord, vurdering: String) {
        try {
            persistenceService.lagreLovmeResponse(soknadRecord.key!!, MedlemskapVurdertParser().parse(vurdering))
        } catch (t: Exception) {
            log.error(
                "Teknisk feil ved lagring av LovmeRespons i databasen, - sykmeldingId: ${soknadRecord.key} . melding : ${t.message}",
                kv("callId", soknadRecord.key)
            )
        }
    }
}
