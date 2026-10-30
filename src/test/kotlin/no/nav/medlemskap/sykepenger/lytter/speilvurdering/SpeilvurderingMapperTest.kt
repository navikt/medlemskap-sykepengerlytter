package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Medlemskapsvurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Speilvurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class SpeilvurderingMapperTest {

    @ParameterizedTest
    @EnumSource(Speilsvar::class, names = ["UAVKLART", "UAVKLART_MED_BRUKERSPORSMAAL"])
    fun `uavklart respons inneholder regelbrudd med forklaringer og standardtekst ved manglende forklaring`(svar: Speilsvar) {
        val vurdering = Speilvurdering(
            soknadId = "12345",
            fnr = "test-fnr",
            speilSvar = svar,
            avklaringer = listOf("REGEL_3", "REGEL_8", "REGEL_11_2_2", "REGEL_23", "SP6120", "SP6130", "UKJENT_REGEL"),
            kanal = "kafka"
        )

        val response = SpeilvurderingMapper().tilSpeilResponse(vurdering)

        Assertions.assertEquals(vurdering.soknadId, response.soknadId)
        Assertions.assertEquals(vurdering.fnr, response.fnr)
        Assertions.assertEquals(svar, response.speilSvar)
        Assertions.assertEquals(
            listOf(
                Aarsak("REGEL_3", "Bruker har ikke hatt et sammenhengende arbeidsforhold i Aa-registeret de siste 12 månedene."),
                Aarsak("REGEL_8", "Bruker er pilot eller kabinansatt. Det er mulig bruker har hjemmebase utenfor Norge."),
                Aarsak("REGEL_11_2_2", "Bruker har barn både i Norge og i utlandet. Vurder om bruker kan ha et annet land enn Norge som lovvalgsland."),
                Aarsak("REGEL_23", "UDI oppga at bruker ikke hadde gyldig oppholdstillatelse da LovMe gjorde vurderingen. Kontroller brukers oppholdstillatelse og arbeidsadgang."),
                Aarsak(
                    "SP6120",
                    "Bruker oppgir å ha vært i arbeid utenfor Norge i søknad. " +
                        "Se første søknad for skjæringspunktet."
                ),
                Aarsak(
                    "SP6130",
                    "Bruker har svart at hen har utført arbeid utenfor Norge i søknad om sykepenger. " +
                        "Det finnes ingen tilleggsinformasjon i søknaden."
                ),
                Aarsak("UKJENT_REGEL", "Kontakt Team LovMe for mer informasjon.")
            ),
            response.aarsaker
        )
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "REGEL_11_2", "REGEL_4", "REGEL_1_3_4", "REGEL_11_5_2",
        "REGEL_0_5", "REGEL_11_2_2_2", "REGEL_11_5_3", "SP6110", "SP6311", "SP6411",
        "SP6229_1", "SP6510", "SP6600", "SP6700"
    ])
    fun `regler uten egen forklaring bruker standardtekst og beholder regelId`(regelId: String) {
        Assertions.assertEquals(
            Aarsak(regelId, "Kontakt Team LovMe for mer informasjon."),
            tilAarsak(regelId)
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["SP6212", "SP6225", "SP6226"])
    fun `oppholdstillatelse i soknad gir tilhorende SP-forklaring`(regelId: String) {
        Assertions.assertEquals(
            Aarsak(
                regelId,
                "Bruker har oppgitt informasjon om oppholdstillatelse i søknad. " +
                    "Se første søknad for skjæringspunktet."
            ),
            tilAarsak(regelId)
        )
    }

    @ParameterizedTest
    @CsvSource("SP6222, Startdatoen", "SP6223, Vedtaksdatoen")
    fun `tidligere oppholdstillatelse gir forklaring med riktig datotype`(regelId: String, datotype: String) {
        Assertions.assertEquals(
            Aarsak(
                regelId,
                "Bruker har oppgitt informasjon om tidligere oppholdstillatelse i søknad. " +
                    "$datotype bruker oppgir er mer enn 12 måneder tilbake i tid."
            ),
            tilAarsak(regelId)
        )
    }

    @Test
    fun `SP-regler med tidligere oppholdstillatelse og standardtekst mappes i riktig rekkefolge`() {
        val vurdering = Speilvurdering(
            soknadId = "12345",
            fnr = "test-fnr",
            speilSvar = Speilsvar.UAVKLART,
            avklaringer = listOf("SP6223", "SP6222", "SP6229_1"),
            kanal = "kafka"
        )

        val response = SpeilvurderingMapper().tilSpeilResponse(vurdering)

        Assertions.assertEquals(
            listOf(
                Aarsak(
                    "SP6223",
                    "Bruker har oppgitt informasjon om tidligere oppholdstillatelse i søknad. " +
                        "Vedtaksdatoen bruker oppgir er mer enn 12 måneder tilbake i tid."
                ),
                Aarsak(
                    "SP6222",
                    "Bruker har oppgitt informasjon om tidligere oppholdstillatelse i søknad. " +
                        "Startdatoen bruker oppgir er mer enn 12 måneder tilbake i tid."
                ),
                Aarsak("SP6229_1", "Kontakt Team LovMe for mer informasjon.")
            ),
            response.aarsaker
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["SP6301", "SP6312", "SP6314"])
    fun `opphold utenfor EOS gir forklaring med avgrensning etter statsborgerskap`(regelId: String) {
        Assertions.assertEquals(
            Aarsak(
                regelId,
                "Bruker har oppgitt informasjon i søknad om opphold utenfor EØS. " +
                    "Se første søknad for skjæringspunktet. " +
                    "Brukers statsborgerskap tilsier at bruker er omfattet av EØS-avtalen, " +
                    "derfor er opphold i EØS-området ikke tatt med."
            ),
            tilAarsak(regelId)
        )
    }

    @Test
    fun `ferieopphold utenfor EOS gir egen forklaring`() {
        Assertions.assertEquals(
            Aarsak(
                "SP6321",
                "Bruker oppgir ferieopphold utenfor EØS i søknad. " +
                    "Se første søknad for skjæringspunktet. " +
                    "Brukers statsborgerskap tilsier at bruker er omfattet av EØS-avtalen, " +
                    "derfor er opphold i EØS-området ikke tatt med."
            ),
            tilAarsak("SP6321")
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["SP6401", "SP6412", "SP6413", "SP6414"])
    fun `opphold utenfor Norge gir tilhorende SP-forklaring`(regelId: String) {
        Assertions.assertEquals(
            Aarsak(
                regelId,
                "Bruker har oppgitt informasjon i søknad om opphold utenfor Norge. " +
                    "Se første søknad for skjæringspunktet."
            ),
            tilAarsak(regelId)
        )
    }

    @Test
    fun `regler med samme standardforklaring beholdes som separate aarsaker i opprinnelig rekkefolge`() {
        val vurdering = Speilvurdering(
            soknadId = "12345",
            fnr = "test-fnr",
            speilSvar = Speilsvar.UAVKLART,
            avklaringer = listOf("SP6700", "REGEL_11_2", "REGEL_0_5"),
            kanal = "kafka"
        )

        val response = SpeilvurderingMapper().tilSpeilResponse(vurdering)

        Assertions.assertEquals(
            listOf(
                Aarsak("SP6700", "Kontakt Team LovMe for mer informasjon."),
                Aarsak("REGEL_11_2", "Kontakt Team LovMe for mer informasjon."),
                Aarsak("REGEL_0_5", "Kontakt Team LovMe for mer informasjon.")
            ),
            response.aarsaker
        )
    }

    @Test
    fun `flerlinjet forklaring sendes som sammenhengende tekst uten ekstra mellomrom`() {
        Assertions.assertEquals(
            Aarsak(
                "REGEL_30",
                "Bruker er statsborger i UK, men har oppholdstype \"EØS eller EFTA\" " +
                    "Kontroller brukers oppholdstillatelse og arbeidsadgang."
            ),
            tilAarsak("REGEL_30")
        )
    }

    @ParameterizedTest
    @EnumSource(Speilsvar::class, names = ["JA", "NEI"])
    fun `ja og nei respons har tom aarsaksliste`(svar: Speilsvar) {
        val vurdering = Speilvurdering(
            soknadId = "12345",
            fnr = "test-fnr",
            speilSvar = svar,
            avklaringer = listOf("REGEL_3"),
            kanal = "kafka"
        )

        val response = SpeilvurderingMapper().tilSpeilResponse(vurdering)

        Assertions.assertEquals(svar, response.speilSvar)
        Assertions.assertTrue(response.aarsaker.isEmpty())
    }

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