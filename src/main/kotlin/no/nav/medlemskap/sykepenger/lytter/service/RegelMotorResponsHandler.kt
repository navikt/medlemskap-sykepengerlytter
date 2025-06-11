package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Periode
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar
import java.time.LocalDate

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

    fun hentOppholdsTilatelsePeriode(lovmeresponse: String): Periode? {
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
            val erEØSborger = medlemskapVurdering.erEosBorger()
            val erTredjelandsborger = medlemskapVurdering.erTredjelandsborger()
            val erTredjelandsborgerMedEØSfamilie = medlemskapVurdering.erTredjelandsborgerMedEØSFamilie()
            val harOppholdtillatelse = medlemskapVurdering.harOppholdsTilatelse()

            val brukerspørsmål: Set<Spørsmål> = when {
                erEØSborger -> setOf(
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE
                )

                erTredjelandsborgerMedEØSfamilie && harOppholdtillatelse -> setOf(
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE
                )

                erTredjelandsborgerMedEØSfamilie && !harOppholdtillatelse -> setOf(
                    Spørsmål.OPPHOLDSTILATELSE,
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE
                )

                erTredjelandsborger && !harOppholdtillatelse -> setOf(
                    Spørsmål.OPPHOLDSTILATELSE,
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_NORGE
                )

                erTredjelandsborger && harOppholdtillatelse -> setOf(
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_NORGE
                )

                else -> emptySet()
            }

            return FlexRespons(svar = Svar.UAVKLART, sporsmal = brukerspørsmål)
        }
        return FlexRespons(svar = Svar.UAVKLART, sporsmal = emptySet())
    }

    private fun MedlemskapVurdering.erEosBorger(): Boolean {
        return this.finnSvarPaaRegel("REGEL_2")
    }

    private fun MedlemskapVurdering.finnSvarPaaRegelFlyt(regelID: String): Boolean {
        try {
            val delresultat = this.resultat.delresultat
                .firstOrNull { it.regelId == regelID }

            return delresultat?.svar == "JA"
        } catch (e: Exception) {
            return false
        }
    }

    private fun MedlemskapVurdering.finnSvarPaaRegel(regelID: String): Boolean {
        val regel = this.alleRegelResultat().firstOrNull { it.regelId == regelID }
        return regel?.svar == "JA"
    }

    private fun MedlemskapVurdering.alleRegelResultat(): List<Delresultat> {
        return this.resultat.delresultat.flatMap { it.delresultat ?: emptyList() }
    }

    private fun MedlemskapVurdering.erTredjelandsborgerMedEØSFamilie(): Boolean {
        return finnSvarPaaRegel("REGEL_28") && finnSvarPaaRegel("REGEL_29")
    }

    private fun MedlemskapVurdering.erTredjelandsborger(): Boolean {
        return !this.finnSvarPaaRegel("REGEL_2")
    }

    private fun MedlemskapVurdering.harOppholdsTilatelse(): Boolean {
        if (finnSvarPaaRegelFlyt("REGEL_OPPHOLDSTILLATELSE")) {
            return true
        }

        // Sjekk uavklart svar fra UDI
        if (this.finnSvarPaaRegel("REGEL_19_1")) {
            return false
        }

        // Sjekk Oppholdstilatelse tilbake i tid
        if (!this.finnSvarPaaRegel("REGEL_19_3")) {
            return false
        }

        // Sjekk oppholdstilatelsen i arbeidsperioden
        if (!this.finnSvarPaaRegel("REGEL_19_3_1")) {
            return false
        }

        // Har bruker opphold på samme vilkår flagg?
        if (this.finnSvarPaaRegel("REGEL_19_8")) {
            return false
        }

        return true
    }
}