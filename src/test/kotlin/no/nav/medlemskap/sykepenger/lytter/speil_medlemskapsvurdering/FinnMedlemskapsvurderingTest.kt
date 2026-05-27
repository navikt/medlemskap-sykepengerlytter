package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import no.nav.medlemskap.sykepenger.lytter.domain.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.domain.UtfortAarbeidUtenforNorge
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FinnMedlemskapsvurderingTest {

    @Test
    fun `lager JA speilrespons basert paa status naar konklusjon finnes`() {
        val speilRespons = FinnMedlemskapsvurdering().finn(vurdering(svar = "UAVKLART", status = "JA"))

        assertEquals(forventetSpeilRespons(Speilsvar.JA), speilRespons)
    }

    @Test
    fun `lager NEI speilrespons basert paa status naar konklusjon finnes`() {
        val speilRespons = FinnMedlemskapsvurdering().finn(vurdering(svar = "JA", status = "NEI"))

        assertEquals(forventetSpeilRespons(Speilsvar.NEI), speilRespons)
    }

    @Test
    fun `lager UAVKLART speilrespons basert paa svar naar konklusjon mangler`() {
        val speilRespons = FinnMedlemskapsvurdering().finn(vurdering(svar = "UAVKLART", status = ""))

        assertEquals(forventetSpeilRespons(Speilsvar.UAVKLART), speilRespons)
    }

    @Test
    fun `lager JA speilrespons basert paa svar naar konklusjon mangler`() {
        val speilRespons = FinnMedlemskapsvurdering().finn(vurdering(svar = "JA", status = ""))

        assertEquals(forventetSpeilRespons(Speilsvar.JA), speilRespons)
    }

    @Test
    fun `lager NEI speilrespons basert paa svar naar konklusjon mangler`() {
        val speilRespons = FinnMedlemskapsvurdering().finn(vurdering(svar = "NEI", status = ""))

        assertEquals(forventetSpeilRespons(Speilsvar.NEI), speilRespons)
    }

    @Test
    fun `lager UAVKLART_MED_BRUKERSPORSMAAL naar svar er uavklart og utfort arbeid utenfor norge finnes`() {
        val speilRespons = FinnMedlemskapsvurdering().finn(
            vurdering(
                svar = "UAVKLART",
                status = "",
                brukerinput = brukerinputMedUtfortArbeidUtenforNorge(),
            )
        )

        assertEquals(forventetSpeilRespons(Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL), speilRespons)
    }

    @Test
    fun `lager UAVKLART_MED_BRUKERSPORSMAAL naar status er uavklart og utfort arbeid utenfor norge finnes`() {
        val speilRespons = FinnMedlemskapsvurdering().finn(
            vurdering(
                svar = "JA",
                status = "UAVKLART",
                brukerinput = brukerinputMedUtfortArbeidUtenforNorge(),
            )
        )

        assertEquals(forventetSpeilRespons(Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL), speilRespons)
    }

    @Test
    fun `ignorerer medlemskapsvurdering som ikke kommer fra kafka kanal`() {
        assertNull(FinnMedlemskapsvurdering().finn(vurdering(kanal = "api", svar = "UAVKLART", status = "UAVKLART")))
    }

    @Test
    fun `ignorerer medlemskapsvurdering som ikke gjelder sykepenger`() {
        assertNull(FinnMedlemskapsvurdering().finn(vurdering(ytelse = "PLEIEPENGER", svar = "UAVKLART", status = "UAVKLART")))
    }

    private fun vurdering(
        kanal: String = "kafka",
        ytelse: String = "SYKEPENGER",
        svar: String,
        status: String,
        brukerinput: Brukerinput = Brukerinput(arbeidUtenforNorge = true),
    ) = MedlemskapVurdering(
        vurderingsId = "bf731267-2c77-3117-9579-3c195ef26602",
        kanal = kanal,
        fnr = "10507213737",
        ytelse = ytelse,
        brukerinput = brukerinput,
        svar = svar,
        status = status,
    )

    private fun brukerinputMedUtfortArbeidUtenforNorge() =
        Brukerinput(
            arbeidUtenforNorge = true,
            utfortAarbeidUtenforNorge = UtfortAarbeidUtenforNorge(
                id = "1b5b87e2-7d83-350e-86dc-2e53b2e23099",
                sporsmalstekst = "Har du arbeidet utenfor Norge?",
                svar = true,
                arbeidUtenforNorge = emptyList(),
            ),
        )

    private fun forventetSpeilRespons(speilsvar: Speilsvar) =
        SpeilRespons("bf731267-2c77-3117-9579-3c195ef26602", "10507213737", speilsvar)
}
