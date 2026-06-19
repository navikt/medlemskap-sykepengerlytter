package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO

object Mottakskriterier {
    fun erOppfylt(sykepengeSoknad: LovmeSoknadDTO): Boolean =
        sykepengeSoknad.type == SoknadstypeDTO.ARBEIDSTAKERE ||
            sykepengeSoknad.type == SoknadstypeDTO.GRADERT_REISETILSKUDD
}
