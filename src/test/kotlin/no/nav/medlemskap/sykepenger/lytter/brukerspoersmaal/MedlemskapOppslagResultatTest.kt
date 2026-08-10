package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MedlemskapOppslagResultatTest {

    @Test
    fun `mapper gradert adresse til eget resultat`() {
        assertEquals(
            MedlemskapOppslagResultat.GradertAdresse,
            MedlemskapOppslagResultat.fra("GradertAdresse")
        )
    }

    @Test
    fun `mapper timeout til eget resultat`() {
        assertEquals(
            MedlemskapOppslagResultat.Tidsavbrudd,
            MedlemskapOppslagResultat.fra("TimeoutCancellationException")
        )
    }

    @Test
    fun `beholder ordinær vurdering i resultatet`() {
        assertEquals(
            MedlemskapOppslagResultat.Vurdering("""{"resultat":"JA"}"""),
            MedlemskapOppslagResultat.fra("""{"resultat":"JA"}""")
        )
    }
}
