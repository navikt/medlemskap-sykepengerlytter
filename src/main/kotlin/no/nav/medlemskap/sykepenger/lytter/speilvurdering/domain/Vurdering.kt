package no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain

sealed interface Vurdering {
    data class VurderingFunnet(val vurdering: Medlemskapsvurdering) : Vurdering
    data object VurderingIkkeFunnet : Vurdering
}
