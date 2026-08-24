package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class BrukersvarDuplikatsjekk(
    private val persistenceService: PersistenceService
) {
    fun erLagretFraFør(brukerspørsmål: Brukerspørsmål): Boolean =
        persistenceService.hentbrukersporsmaalForSoknadID(brukerspørsmål.soknadid) != null
}
