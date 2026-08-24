package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Medlemskapsvurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SpeilvurderingMapperTest {

    @Test
    fun `ja svar skal være case insensitive`() {
        val mapper = SpeilvurderingMapper()
        val vurdering = Medlemskapsvurdering(
            JacksonParser().ToJson(
                """
                {
                  "datagrunnlag": {
                    "fnr": "98765434567",
                    "brukerinput": {}
                  },
                  "resultat": {
                    "svar": "ja"
                  }
                }
                """.trimIndent()
            )
        )

        Assertions.assertEquals(
            Speilsvar.JA,
            mapper.fraSaga(vurdering, "1").speilSvar
        )
    }

    @Test
    fun `nei svar skal være case insensitive`() {
        val vurdering = Medlemskapsvurdering(
            JacksonParser().ToJson(
                """
                {
                  "datagrunnlag": {
                    "fnr": "98765434567",
                    "brukerinput": {}
                  },
                  "resultat": {
                    "svar": "nei"
                  }
                }
                """.trimIndent()
            )
        )

        Assertions.assertEquals(
            Speilsvar.NEI,
            SpeilvurderingMapper().fraSaga(vurdering, "1").speilSvar
        )
    }

    @Test
    fun `Ja svar i konklusjon med uavklart i resultat skal svare JA`(){
        val fileContent = this::class.java.classLoader.getResource("SagaResponsHaleKorrigeringTilJa.json").readText(Charsets.UTF_8)
        val vurdering = SpeilvurderingMapper().fraSaga(
            Medlemskapsvurdering(JacksonParser().ToJson(fileContent)),
            "1"
        )
        val avklaringer = vurdering.avklaringer
        Assertions.assertTrue(avklaringer.isEmpty())
        Assertions.assertEquals(Speilsvar.JA, vurdering.speilSvar, "Feil svar : Konklusjon skal benyttes i svar")
        Assertions.assertEquals("98765434567", vurdering.fnr, "Feil mapping av fnr")
        Assertions.assertEquals("1", vurdering.soknadId, "når ikke vurderingsID finnes i json skal default brukes")
    }
    @Test
    fun `Uavklart uten brukerspormsaal skal svare UAVKLART`(){
        val fileContent = this::class.java.classLoader.getResource("SagaResponsUavklartUtenBrukerSvar.json").readText(Charsets.UTF_8)
        val vurdering = SpeilvurderingMapper().fraSaga(
            Medlemskapsvurdering(JacksonParser().ToJson(fileContent)),
            "1"
        )
        Assertions.assertEquals(Speilsvar.UAVKLART, vurdering.speilSvar, "Feil svar : Konklusjon skal benyttes i svar")
    }
    @Test
    fun `Uavklart MED brukerspormsaal skal svare UAVKLART_MED_BRUKERSPORSMAAL`(){
        val fileContent = this::class.java.classLoader.getResource("SagaResponsUavklartMedBrukerSporsmaal.json").readText(Charsets.UTF_8)
        val vurdering = SpeilvurderingMapper().fraSaga(
            Medlemskapsvurdering(JacksonParser().ToJson(fileContent)),
            "1"
        )
        Assertions.assertFalse(vurdering.avklaringer.isEmpty())
        Assertions.assertEquals(Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL, vurdering.speilSvar, "Feil svar : Konklusjon skal benyttes i svar")
    }
    @Test
    fun `hentAvklaringer Skal ikke feile selv på gammel modell`(){
        val fileContent = this::class.java.classLoader.getResource("sampleVurdering_uavklart_REGEL_C.json").readText(Charsets.UTF_8)
        Assertions.assertTrue(
            SpeilvurderingMapper().fraSaga(
                Medlemskapsvurdering(JacksonParser().ToJson(fileContent)),
                "1"
            ).avklaringer.isNotEmpty()
        )

    }
    @Test
    fun `vurderingsID skal hentes fra Json dersom det finnes`(){
        val fileContent = this::class.java.classLoader.getResource("SagaResponsUavklartMedBrukerSporsmaalOgVurderingsId.json").readText(Charsets.UTF_8)
        val vurdering = SpeilvurderingMapper().fraSaga(
            Medlemskapsvurdering(JacksonParser().ToJson(fileContent)),
            "1"
        )
        Assertions.assertEquals(Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL, vurdering.speilSvar, "Feil svar : Konklusjon skal benyttes i svar")
        Assertions.assertEquals("ed0286f6-6107-3d75-8266-e50d5736f403", vurdering.soknadId)
    }
}