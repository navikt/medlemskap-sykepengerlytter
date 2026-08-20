package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import com.fasterxml.jackson.databind.JsonNode
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.inneholderNyModell
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapOppslagVurdering
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Medlemskapsvurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Speilvurdering

class SpeilvurderingMapper {
    fun tilSpeilResponse(vurdering: Speilvurdering): SpeilResponse =
        SpeilResponse(
            soknadId = vurdering.soknadId,
            fnr = vurdering.fnr,
            speilSvar = vurdering.speilSvar
        )

    fun fraSaga(vurdering: Medlemskapsvurdering, callId: String): Speilvurdering {
        val brukerinput = JacksonParser().toDomainObject<Brukerinput>(
            vurdering.json.path("datagrunnlag").path("brukerinput")
        )
        return Speilvurdering(
            soknadId = vurdering.json.path("vurderingsID").asText(callId),
            fnr = vurdering.json.path("datagrunnlag").path("fnr").asText(),
            speilSvar = finnSpeilSvar(finnSvar(vurdering.json), brukerinput),
            avklaringer = finnAvklaringer(vurdering.json),
            kanal = vurdering.json.path("kanal").asText("Ukjent")
        )
    }

    fun fraMedlemskapOppslag(vurdering: MedlemskapOppslagVurdering, callId: String): Speilvurdering {
        val brukerinput = JacksonParser().toDomainObject<Brukerinput>(vurdering.datagrunnlag.path("brukerinput"))
        return Speilvurdering(
            soknadId = vurdering.vurderingsID ?: callId,
            fnr = vurdering.datagrunnlag.get("fnr").asText(),
            speilSvar = finnSpeilSvar(vurdering.resultat.path("svar").asText(), brukerinput),
            avklaringer = vurdering.resultat.get("årsaker")
                .toList()
                .map { it.get("regelId").asText() },
            kanal = vurdering.kanal ?: "Ukjent"
        )
    }

    private fun finnSpeilSvar(svar: String, brukerinput: Brukerinput): Speilsvar =
        when {
            svar.equals("JA", ignoreCase = true) -> Speilsvar.JA
            svar.equals("NEI", ignoreCase = true) -> Speilsvar.NEI
            brukerinput.inneholderNyModell() -> Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL
            else -> Speilsvar.UAVKLART
        }

    private fun finnSvar(vurdering: JsonNode): String {
        val status = vurdering.path("konklusjon").path(0).path("status")
        return if (!status.isMissingNode && !status.isNull) {
            status.asText()
        } else {
            vurdering.path("resultat").path("svar").asText()
        }
    }

    private fun finnAvklaringer(vurdering: JsonNode): List<String> {
        val avklaringsliste = vurdering.path("konklusjon").path(0).path("avklaringsListe")
        return if (avklaringsliste.isArray) {
            avklaringsliste.map { it.path("regel_id").asText() }
        } else {
            vurdering.path("resultat").path("årsaker")
                .map { it.path("regelId").asText() }
        }
    }
}