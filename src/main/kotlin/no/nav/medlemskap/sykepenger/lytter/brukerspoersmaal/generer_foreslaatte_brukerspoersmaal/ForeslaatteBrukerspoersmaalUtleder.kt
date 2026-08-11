package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.generer_foreslaatte_brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.MedlemskapVurderingMapper
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.flexrespons.tilFlexRespons
import no.nav.medlemskap.sykepenger.lytter.domain.Delresultat
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapVurdering
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.Spørsmål

class ForeslaatteBrukerspoersmaalUtleder(
    private val medlemskapVurderingMapper: MedlemskapVurderingMapper = MedlemskapVurderingMapper()
) {

    fun utledResultat(medlemskapOppslagResponse: String): FlexRespons {
        val medlemskapVurdering = medlemskapVurderingMapper.map(medlemskapOppslagResponse)
        return tilForeslåttBrukerspørsmål(medlemskapVurdering).tilFlexRespons(medlemskapVurdering)
    }

    fun tilForeslåttBrukerspørsmål(medlemskapVurdering: MedlemskapVurdering): Set<Spørsmål> {
        return when (medlemskapVurdering.resultat.svar) {
            "UAVKLART" -> håndterBrukerspørsmål(medlemskapVurdering)
            "JA", "NEI" -> emptySet()
            else -> throw IllegalStateException()
        }
    }

    private fun håndterBrukerspørsmål(medlemskapVurdering: MedlemskapVurdering): Set<Spørsmål> {
        val årsaker = medlemskapVurdering.resultat.årsaker.map { it.regelId }

        if (RegelbruddSomGirBrukerspoersmaal().skalGiBrukerspørsmål(årsaker)) {
            val erEØSborger = medlemskapVurdering.erEØSBorger()
            val erAndreBorgere = medlemskapVurdering.erAndreBorgere()
            val erAndreBorgereMedEØSfamilie = medlemskapVurdering.erAndreBorgereMedEØSFamilie()
            val harOppholdsTillatelse = medlemskapVurdering.harOppholdsTillatelse()

            val harBruddPåRegel23 = harBruddPåRegel23(årsaker)

            val brukerspørsmål: Set<Spørsmål> = when {
                erEØSborger -> setOf(
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE
                )

                erAndreBorgereMedEØSfamilie && harOppholdsTillatelse -> setOf(
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE
                )

                //Unngå å stille spørsmål om oppholdstillatelse ved brudd på regel 23
                erAndreBorgereMedEØSfamilie && harBruddPåRegel23 -> setOf(
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE
                )

                erAndreBorgereMedEØSfamilie && !harOppholdsTillatelse -> setOf(
                    Spørsmål.OPPHOLDSTILATELSE,
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE
                )

                erAndreBorgere && harOppholdsTillatelse -> setOf(
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_NORGE
                )

                //Unngå å stille spørsmål om oppholdstillatelse ved brudd på regel 23
                erAndreBorgere && harBruddPåRegel23 -> setOf(
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE
                )

                erAndreBorgere && !harOppholdsTillatelse -> setOf(
                    Spørsmål.OPPHOLDSTILATELSE,
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_NORGE
                )

                else -> emptySet()
            }

            return brukerspørsmål
        }
        return emptySet()
    }

    private fun MedlemskapVurdering.erEØSBorger(): Boolean {
        return this.erSvarPåRegelJa("REGEL_2")
    }

    private fun MedlemskapVurdering.erSvarPåRegelJa(regelID: String): Boolean {
        val regel = this.alleRegelResultat().firstOrNull { it.regelId == regelID }
        return regel?.svar == "JA"
    }

    private fun MedlemskapVurdering.alleRegelResultat(): List<Delresultat> {
        return this.resultat.delresultat.flatMap { it.delresultat ?: emptyList() }
    }

    private fun MedlemskapVurdering.erAndreBorgereMedEØSFamilie(): Boolean {
        return erSvarPåRegelJa("REGEL_28") && erSvarPåRegelJa("REGEL_29")
    }

    private fun MedlemskapVurdering.erAndreBorgere(): Boolean {
        return !this.erSvarPåRegelJa("REGEL_2")
    }

    private fun MedlemskapVurdering.harOppholdsTillatelse(): Boolean {
        return this.erSvarPåRegelJa("REGEL_19_3")
    }

    private fun harBruddPåRegel23(årsaker: List<String>): Boolean {
        return årsaker.contains("REGEL_23")
    }
}