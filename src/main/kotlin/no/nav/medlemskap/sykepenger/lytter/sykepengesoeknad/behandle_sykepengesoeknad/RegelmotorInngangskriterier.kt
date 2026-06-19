package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadsstatusDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO

object RegelmotorInngangskriterier {

    private val tillatteSoknadstyper = setOf(
        SoknadstypeDTO.ARBEIDSTAKERE,
        SoknadstypeDTO.GRADERT_REISETILSKUDD
    )

    fun erOppfylt(soknad: LovmeSoknadDTO): Boolean =
        soknad.status == SoknadsstatusDTO.SENDT.name &&
                soknad.type in tillatteSoknadstyper &&
                !soknad.ettersending!!
}