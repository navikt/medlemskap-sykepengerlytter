package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class BrukersvarDuplikatsjekk(
    private val persistenceService: PersistenceService
) {
    fun erLagretFraFør(brukersporsmaal: Brukersporsmaal): Boolean =
        persistenceService.hentbrukersporsmaalForSoknadID(brukersporsmaal.soknadid) != null
}
