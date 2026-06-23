package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.domain.erFunkskjoneltLik
import no.nav.medlemskap.sykepenger.lytter.domain.erpåfølgende
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class SykepengesoeknadFiltrering(
    private val persistenceService: PersistenceService
) {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    fun finnDuplikatSomSkalFiltreres(sykepengeSoknad: LovmeSoknadDTO): Boolean {
        val medlemRequest = mapToMedlemskap(sykepengeSoknad)
        val duplikat = erDuplikat(medlemRequest)
        return duplikat != null && arbeidUtenForNorgeFalse(sykepengeSoknad)
    }

    fun erDuplikat(medlemRequest: Medlemskap): Medlemskap? {
        val vurderinger = persistenceService.hentMedlemskap(medlemRequest.fnr)
        return vurderinger.find { medlemRequest.erFunkskjoneltLik(it) }
    }

    fun erPåfølgendeSøknad(sykepengeSoknad: LovmeSoknadDTO): Boolean {
        if (true == sykepengeSoknad.forstegangssoknad) {
            return false
        }
        val medlemRequest = mapToMedlemskap(sykepengeSoknad)
        val vurderinger = persistenceService.hentMedlemskap(sykepengeSoknad.fnr)
        val result = vurderinger.find { medlemRequest.erpåfølgende(it) }
        return result != null && arbeidUtenForNorgeFalse(sykepengeSoknad)
    }

    fun lagreHvisPåfølgendeSøknad(sykepengeSoknad: LovmeSoknadDTO): Boolean {
        if (!erPåfølgendeSøknad(sykepengeSoknad)) {
            return false
        }

        persistenceService.lagrePaafolgendeSoknad(sykepengeSoknad)
        return true
    }

    private fun arbeidUtenForNorgeFalse(sykepengeSoknad: LovmeSoknadDTO): Boolean {
        if (sykepengeSoknad.arbeidUtenforNorge == true) {
            log.info(
                "Søknad inneholder arbeidUtenforNorge=true og skal ikke filtreres - sykmeldingId: ${sykepengeSoknad.id}",
                kv("callId", sykepengeSoknad.id),
            )
        }
        return sykepengeSoknad.arbeidUtenforNorge == false || sykepengeSoknad.arbeidUtenforNorge == null
    }

    private fun mapToMedlemskap(sykepengeSoknad: LovmeSoknadDTO): Medlemskap =
        Medlemskap(sykepengeSoknad.fnr, sykepengeSoknad.fom!!, sykepengeSoknad.tom!!, ErMedlem.UAVKLART)
}
