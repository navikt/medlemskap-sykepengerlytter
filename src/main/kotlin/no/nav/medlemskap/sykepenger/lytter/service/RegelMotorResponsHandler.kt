package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Periode
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar
import java.time.LocalDate


class RegelMotorResponsHandler {

    fun utledResultat(medlemskapsVurdering: String): FlexRespons {
        val medlemskapsVurderingen = objectMapper.readTree(medlemskapsVurdering)
        when (medlemskapsVurderingen.svar()) {
            "UAVKLART" -> return håndterBrukerspørsmål(medlemskapsVurderingen)
            "JA" -> return FlexRespons(svar = Svar.JA, emptySet())
            "NEI" -> return FlexRespons(svar = Svar.NEI, emptySet())
            else -> throw IllegalStateException()
        }
    }

    fun hentOppholdsTilatelsePeriode(lovmeresponse: String): Periode? {
        val lovmeresponseNode = objectMapper.readTree(lovmeresponse)
        val periode = lovmeresponseNode.oppholdsTillatelsePeriode()
        if (periode != null) {
            val fom: LocalDate = LocalDate.parse(periode.get("fom").asText())
            val tom: LocalDate? = runCatching {
                LocalDate.parse(periode.get("tom").asText())
            }
                .getOrNull()
            return Periode(fom, tom)
        }
        return null
    }


    private fun håndterBrukerspørsmål(respons: JsonNode): FlexRespons {
        val årsaker = respons.aarsaker()
        if (GenererBrukerSporsmaal().skalGenerereBrukerSpørsmål(årsaker)) {

            val erEØSborger = respons.erEosBorger()
            val erTredjelandsborger = respons.erTredjelandsborger()
            val erTredjelandsborgerMedEØSfamilie = respons.erTredjelandsborgerMedEØSFamilie()
            val harOppholdtillatelse = respons.harOppholdsTilatelse()

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


    fun JsonNode.erEosBorger(): Boolean {
        return this.finnSvarPaaRegel("REGEL_2")
    }


    fun JsonNode.finnSvarPaaRegelFlyt(regelID: String): Boolean {
        try {

            val svar = this.get("resultat").get("delresultat")
                .filter { it.get("regelId").asText().equals(regelID) }.first().get("svar").asText()

            if (svar.equals("JA")) {
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }


    fun JsonNode.finnSvarPaaRegel(regelID: String): Boolean {
        val regel = this.alleRegelResultat().finnRegel(regelID)
        if (regel != null) {
            return regel.get("svar").asText() == "JA"
        }
        return false
    }

    fun JsonNode.alleRegelResultat(): List<JsonNode> {
        return this.get("resultat").get("delresultat").flatMap { it.get("delresultat") }
    }

    fun JsonNode.aarsaker(): List<String> {
        return this.get("resultat").get("årsaker").map { it.get("regelId").asText() }
    }


    fun List<JsonNode>.finnRegel(regelID: String): JsonNode? {
        return this.find { it.get("regelId").asText() == regelID }
    }

    fun JsonNode.erTredjelandsborgerMedEØSFamilie(): Boolean {
        return finnSvarPaaRegel("REGEL_28") && finnSvarPaaRegel("REGEL_29")
    }

    fun JsonNode.erTredjelandsborger(): Boolean {
        return !this.finnSvarPaaRegel("REGEL_2")
    }


    fun JsonNode.harOppholdsTilatelse(): Boolean {


        if (finnSvarPaaRegelFlyt("REGEL_OPPHOLDSTILLATELSE")) {
            return true
        }

        /*
        * Sjekk uavklart svar fra UDI
        * */
        if (this.finnSvarPaaRegel("REGEL_19_1")) {
            return false
        }
        /*
        * Sjekk Oppholdstilatelse tilbake i tid
        * */
        if (!this.finnSvarPaaRegel("REGEL_19_3")) {
            return false
        }
        /*
        * Sjekk oppholdstilatelsen i  arbeidsperioden
        * */
        if (!this.finnSvarPaaRegel("REGEL_19_3_1")) {
            return false
        }
        /*
         *Har bruker opphold på samme vilkår flagg?
         */
        if (this.finnSvarPaaRegel("REGEL_19_8")) {
            return false

        }
        return true
    }

    fun JsonNode.svar(): String {
        return this.get("resultat").get("svar").asText()
    }

    /*
    * ment å brukes ved uthenting av perioden for oppholdstilatelser der bruker har gått ut på brudd på regel 19_3.
    * Usikkert om denne vil fungere dersom det ikke er oppholdstillatelse Pa Samme Vilkar
    * */
    fun JsonNode.oppholdsTillatelsePeriode(): JsonNode? {
        runCatching {
            this.get("datagrunnlag").get("oppholdstillatelse").get("gjeldendeOppholdsstatus")
                .get("oppholdstillatelsePaSammeVilkar").get("periode")
        }
            .onSuccess { return it }
            .onFailure { return null }
        return null
    }
}