package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka.SpeilResponsPublisher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MedlemskapsvurderingTilSpeilResponsTest {

    private val handler = MedlemskapsvurderingMapper()

    @Test
    fun `mapper uavklart medlemskapsvurdering til feltene vi trenger`() {
        val vurdering = handler.tilMedlemskapsvurdering(uavklartMelding)

        assertEquals("bf731267-2c77-3117-9579-3c195ef26602", vurdering.vurderingsId)
        assertEquals("kafka", vurdering.kanal)
        assertEquals("10507213737", vurdering.fnr)
        assertEquals("SYKEPENGER", vurdering.ytelse)
        assertEquals(true, vurdering.brukerinput.arbeidUtenforNorge)
        assertEquals(true, vurdering.brukerinput.oppholdUtenforEos?.svar)
        assertEquals("UAVKLART", vurdering.svar)
        assertEquals("UAVKLART", vurdering.status)
    }

    @Test
    fun `mapper ja medlemskapsvurdering til feltene vi trenger`() {
        val vurdering = handler.tilMedlemskapsvurdering(jaMelding)

        assertEquals("a01b6a2c-8d9b-3d87-8a2e-5c888350f148", vurdering.vurderingsId)
        assertEquals("kafka", vurdering.kanal)
        assertEquals("11897898049", vurdering.fnr)
        assertEquals("SYKEPENGER", vurdering.ytelse)
        assertEquals(false, vurdering.brukerinput.arbeidUtenforNorge)
        assertEquals("JA", vurdering.svar)
        assertEquals("JA", vurdering.status)
    }

    @Test
    fun `setter tom status naar konklusjon ikke finnes`() {
        val vurdering = handler.tilMedlemskapsvurdering(meldingUtenKonklusjon)

        assertEquals("a01b6a2c-8d9b-3d87-8a2e-5c888350f148", vurdering.vurderingsId)
        assertEquals("JA", vurdering.svar)
        assertEquals("", vurdering.status)
    }

    @Test
    fun `publiserer SpeilRespons naar medlemskapsvurdering skal til Speil`() {
        val publiserteSvar = mutableListOf<SpeilRespons>()
        val meldingsbehandler = SpeilResponsBehandler(
            speilResponsPublisher = SpeilResponsPublisher { publiserteSvar.add(it) }
        )

        meldingsbehandler.behandle(
            ConsumerRecord("medlemskap.medlemskap-vurdert", 0, 0, "callId", jaMelding)
        )

        val forventetRespons = SpeilRespons("a01b6a2c-8d9b-3d87-8a2e-5c888350f148", "11897898049", Speilsvar.JA)
        assertEquals(listOf(forventetRespons), publiserteSvar)
    }

    @Test
    fun `publiserer ikke naar medlemskapsvurdering ikke skal til Speil`() {
        val publiserteSvar = mutableListOf<SpeilRespons>()
        val meldingsbehandler = SpeilResponsBehandler(
            speilResponsPublisher = SpeilResponsPublisher { publiserteSvar.add(it) }
        )

        meldingsbehandler.behandle(
            ConsumerRecord(
                "medlemskap.medlemskap-vurdert",
                0,
                0,
                "callId",
                jaMelding.replace("\"ytelse\": \"SYKEPENGER\"", "\"ytelse\": \"PLEIEPENGER\"")
            )
        )

        assertEquals(emptyList<SpeilRespons>(), publiserteSvar)
    }

    private val uavklartMelding = """
        {
          "vurderingsID": "bf731267-2c77-3117-9579-3c195ef26602",
          "kanal": "kafka",
          "resultat": {
            "svar": "UAVKLART",
            "dekning": "",
            "regelId": "REGEL_MEDLEM_KONKLUSJON"
          },
          "konklusjon": [
            {
              "status": "UAVKLART",
              "avklaringsListe": []
            }
          ],
          "datagrunnlag": {
            "fnr": "10507213737",
            "ytelse": "SYKEPENGER",
            "brukerinput": {
              "oppholdUtenforEos": {
                "id": "6daa94ba-df7f-35cd-8634-5f453ff56a08",
                "svar": true,
                "sporsmalstekst": "Har du oppholdt deg utenfor EOS?",
                "oppholdUtenforEOS": []
              },
              "oppholdstilatelse": null,
              "arbeidUtenforNorge": true,
              "oppholdUtenforNorge": null,
              "utfortAarbeidUtenforNorge": null
            }
          }
        }
    """.trimIndent()

    private val jaMelding = """
        {
          "vurderingsID": "a01b6a2c-8d9b-3d87-8a2e-5c888350f148",
          "kanal": "kafka",
          "resultat": {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_MEDLEM_KONKLUSJON"
          },
          "konklusjon": [
            {
              "status": "JA",
              "avklaringsListe": []
            }
          ],
          "datagrunnlag": {
            "fnr": "11897898049",
            "ytelse": "SYKEPENGER",
            "brukerinput": {
              "oppholdUtenforEos": null,
              "oppholdstilatelse": null,
              "arbeidUtenforNorge": false,
              "oppholdUtenforNorge": null,
              "utfortAarbeidUtenforNorge": null
            }
          }
        }
    """.trimIndent()

    private val meldingUtenKonklusjon = """
        {
          "vurderingsID": "a01b6a2c-8d9b-3d87-8a2e-5c888350f148",
          "kanal": "kafka",
          "resultat": {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_MEDLEM_KONKLUSJON"
          },
          "datagrunnlag": {
            "fnr": "11897898049",
            "ytelse": "SYKEPENGER",
            "brukerinput": {
              "oppholdUtenforEos": null,
              "oppholdstilatelse": null,
              "arbeidUtenforNorge": false,
              "oppholdUtenforNorge": null,
              "utfortAarbeidUtenforNorge": null
            }
          }
        }
    """.trimIndent()
}
