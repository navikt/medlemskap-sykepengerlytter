package no.nav.medlemskap.sykepenger.lytter.rest

import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SpeilSvarTest {

    @Test
    fun `Ja svar i konklusjon med uavklart i resultat skal svare JA`(){
        val fileContent = this::class.java.classLoader.getResource("SagaResponsHaleKorrigeringTilJa.json").readText(Charsets.UTF_8)
        val json = JacksonParser().ToJson(fileContent)
        val speilResponse = json.lagSpeilRespons("1")
        val avklaringer = json.hentAvklaringer()
        Assertions.assertTrue(avklaringer.isEmpty())
        Assertions.assertEquals(Speilsvar.JA,speilResponse.speilSvar,"Feil svar : Konklusjon skal benyttes i svar")
        Assertions.assertEquals("98765434567",speilResponse.fnr,"Feil mapping av fnr")
        Assertions.assertEquals("1",speilResponse.soknadId,"når ikke vurderingsID finnes i json skal default brukes")
    }
    @Test
    fun `Uavklart uten brukerspormsaal skal svare UAVKLART`(){
        val fileContent = this::class.java.classLoader.getResource("SagaResponsUavklartUtenBrukerSvar.json").readText(Charsets.UTF_8)
        val json = JacksonParser().ToJson(fileContent)
        val speilResponse = JacksonParser().ToJson(fileContent).lagSpeilRespons("1")
        Assertions.assertEquals(Speilsvar.UAVKLART,speilResponse.speilSvar,"Feil svar : Konklusjon skal benyttes i svar")
    }
    @Test
    fun `Uavklart MED brukerspormsaal skal svare UAVKLART_MED_BRUKERSPORSMAAL`(){
        val fileContent = this::class.java.classLoader.getResource("SagaResponsUavklartMedBrukerSporsmaal.json").readText(Charsets.UTF_8)
        val json = JacksonParser().ToJson(fileContent)
        val speilResponse = JacksonParser().ToJson(fileContent).lagSpeilRespons("1")
        Assertions.assertFalse(json.hentAvklaringer().isEmpty())
        Assertions.assertEquals(Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL,speilResponse.speilSvar,"Feil svar : Konklusjon skal benyttes i svar")
    }
    @Test
    fun `vureringsID skal hentes fra Json dersom det finnes`(){
        val fileContent = this::class.java.classLoader.getResource("SagaResponsUavklartMedBrukerSporsmaalOgVurderingsId.json").readText(Charsets.UTF_8)
        val json = JacksonParser().ToJson(fileContent)
        val speilResponse = JacksonParser().ToJson(fileContent).lagSpeilRespons("1")
        val listeAvAvklaringer = json.hentAvklaringer()
        Assertions.assertEquals(Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL,speilResponse.speilSvar,"Feil svar : Konklusjon skal benyttes i svar")
        Assertions.assertEquals("ed0286f6-6107-3d75-8266-e50d5736f403",speilResponse.soknadId)
    }
}