package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Type
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Status

data class InngangskriterierResultat(
    val erOppfylt: Boolean,
    val brutteKriterier: List<BruttInngangskriterium>
)

enum class BruttInngangskriterium {
    FEIL_STATUS,
    IKKE_TILLATT_TYPE,
    ER_ETTERSENDING
}

object Inngangskriterier {

    private val tillatteSoknadstyper = setOf(
        Type.ARBEIDSTAKERE,
        Type.GRADERT_REISETILSKUDD
    )

    fun erOppfylt(soknad: SykepengesoeknadGrunnlag): Boolean =
        vurder(soknad).erOppfylt

    fun vurder(soknad: SykepengesoeknadGrunnlag): InngangskriterierResultat {
        val brutteKriterier = buildList {
            if (soknad.status != Status.SENDT.name) {
                add(BruttInngangskriterium.FEIL_STATUS)
            }
            if (soknad.type !in tillatteSoknadstyper) {
                add(BruttInngangskriterium.IKKE_TILLATT_TYPE)
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
