package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadsstatusDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO

data class InngangskriterierResultat(
    val erOppfylt: Boolean,
    val brutteKriterier: List<BruttInngangskriterium>
)

enum class BruttInngangskriterium {
    FEIL_STATUS,
    IKKE_TILLATT_SOKNADSTYPE,
    ER_ETTERSENDING
}

object Inngangskriterier {

    private val tillatteSoknadstyper = setOf(
        SoknadstypeDTO.ARBEIDSTAKERE,
        SoknadstypeDTO.GRADERT_REISETILSKUDD
    )

    fun erOppfylt(soknad: LovmeSoknadDTO): Boolean =
        vurder(soknad).erOppfylt

    fun vurder(soknad: LovmeSoknadDTO): InngangskriterierResultat {
        val brutteKriterier = buildList {
            if (soknad.status != SoknadsstatusDTO.SENDT.name) {
                add(BruttInngangskriterium.FEIL_STATUS)
            }
            if (soknad.type !in tillatteSoknadstyper) {
                add(BruttInngangskriterium.IKKE_TILLATT_SOKNADSTYPE)
            }
            if (soknad.ettersending!!) {
                add(BruttInngangskriterium.ER_ETTERSENDING)
            }
        }

        return InngangskriterierResultat(
            erOppfylt = brutteKriterier.isEmpty(),
            brutteKriterier = brutteKriterier
        )
    }
}
