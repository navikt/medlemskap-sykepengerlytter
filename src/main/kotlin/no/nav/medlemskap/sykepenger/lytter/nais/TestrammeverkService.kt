package no.nav.medlemskap.sykepenger.lytter.nais

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class TestrammeverkService(private val persistenceService: PersistenceService) {

    fun finnNyesteBrukersvar(fnr: String): Brukerspørsmål? =
        persistenceService
            .hentbrukersporsmaalForFnr(fnr)
            .maxByOrNull { it.eventDate }
}
