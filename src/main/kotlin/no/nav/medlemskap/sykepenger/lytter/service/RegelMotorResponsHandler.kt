package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Periode
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar

class RegelMotorResponsHandler {

    fun utledResultat(medlemskapsVurdering: String): FlexRespons {
        val medlemskapVurdering = objectMapper.readValue<MedlemskapVurdering>(medlemskapsVurdering)

        when (medlemskapVurdering.resultat.svar) {
            "UAVKLART" -> return håndterBrukerspørsmål(medlemskapVurdering)
            "JA" -> return FlexRespons(svar = Svar.JA, emptySet())
            "NEI" -> return FlexRespons(svar = Svar.NEI, emptySet())
            else -> throw IllegalStateException()
        }
    }

    fun hentOppholdstillatelsePeriode(lovmeresponse: String): Periode? {
        val medlemskapVurdering = objectMapper.readValue<MedlemskapVurdering>(lovmeresponse)

        return medlemskapVurdering
            .datagrunnlag
            ?.oppholdstillatelse
            ?.gjeldendeOppholdsstatus
            ?.oppholdstillatelsePaSammeVilkar
            ?.periode
            ?.let {
                Periode(fom = it.fom, tom = it.tom)
            }
    }

    private fun håndterBrukerspørsmål(medlemskapVurdering: MedlemskapVurdering): FlexRespons {
        val årsaker = medlemskapVurdering.resultat.årsaker.map { it.regelId }

        if (GenererBrukerSporsmaal().skalGenerereBrukerSpørsmål(årsaker)) {
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

            return FlexRespons(svar = Svar.UAVKLART, sporsmal = brukerspørsmål)
        }
        return FlexRespons(svar = Svar.UAVKLART, sporsmal = emptySet())
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