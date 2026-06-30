package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.domain.erFunkskjoneltLik
import no.nav.medlemskap.sykepenger.lytter.domain.erpåfølgende
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import org.slf4j.MarkerFactory

class SykepengesoeknadFiltrering(
    private val persistenceService: PersistenceService
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    fun erDuplikatOgSvartNeiPåArbeidUtenforNorge(sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag): Boolean {
        val medlemRequest = mapToMedlemskap(sykepengesøknadGrunnlag)
        val duplikat = erDuplikat(medlemRequest)
        return duplikat != null && arbeidUtenForNorgeFalse(sykepengesøknadGrunnlag)
    }

    fun erDuplikat(medlemRequest: Medlemskap): Medlemskap? {
        val vurderinger = persistenceService.hentMedlemskap(medlemRequest.fnr)
        return vurderinger.find { medlemRequest.erFunkskjoneltLik(it) }
    }

    fun erPåfølgendeSøknadOgSvartNeiPåArbeidUtenforNorge(sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag): Boolean {
        if (true == sykepengesøknadGrunnlag.forstegangssoknad) {
            return false
        }
        val medlemRequest = mapToMedlemskap(sykepengesøknadGrunnlag)
        val vurderinger = persistenceService.hentMedlemskap(sykepengesøknadGrunnlag.fnr)
        val result = vurderinger.find { medlemRequest.erpåfølgende(it) }
        return result != null && arbeidUtenForNorgeFalse(sykepengesøknadGrunnlag)
    }

    fun lagreHvisPåfølgendeSøknadOgSvartNeiPåArbeidUtenforNorge(sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag): Boolean {
        if (!erPåfølgendeSøknadOgSvartNeiPåArbeidUtenforNorge(sykepengesøknadGrunnlag)) {
            return false
        }

        persistenceService.lagrePaafolgendeSoknad(sykepengesøknadGrunnlag)
        return true
    }

    private fun arbeidUtenForNorgeFalse(sykepengesøknadGrunnlag: SykepengesoeknadGrunnlag): Boolean {
        if (sykepengesøknadGrunnlag.arbeidUtenforNorge == true) {
            log.info(
                teamLogs,
                "Søknad inneholder arbeidUtenforNorge=true og skal ikke filtreres - sykmeldingId: ${sykepengesøknadGrunnlag.id}",
                kv("callId", sykepengesøknadGrunnlag.id),
            )
        }
        return sykepengesøknadGrunnlag.arbeidUtenforNorge == false || sykepengesøknadGrunnlag.arbeidUtenforNorge == null
    }

    private fun mapToMedlemskap(sykepengeSoknad: SykepengesoeknadGrunnlag): Medlemskap =
        Medlemskap(sykepengeSoknad.fnr, sykepengeSoknad.fom!!, sykepengeSoknad.tom!!, ErMedlem.UAVKLART)
}
