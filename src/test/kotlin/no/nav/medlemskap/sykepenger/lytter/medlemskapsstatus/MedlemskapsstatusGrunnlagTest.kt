package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import no.nav.medlemskap.sykepenger.lytter.domain.Status
import no.nav.medlemskap.sykepenger.lytter.domain.Vurderingsstatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MedlemskapsstatusGrunnlagTest {
    @Test
    fun `finner siste tidligere vurdering som ikke er påfølgende`() {
        val vurderinger = listOf(
            vurdering("2024-01-01", "2024-01-10"),
            vurdering("2024-01-11", "2024-01-20"),
            vurdering("2024-01-21", "2024-01-31", Status.PAFOLGENDE),
            vurdering("2024-03-01", "2024-03-10")
        )

        val resultat = vurderinger.finnGrunnlagForFørstegangssøknaden(
            vurdering("2024-02-01", "2024-02-28", Status.PAFOLGENDE)
        )

        assertEquals(vurdering("2024-01-11", "2024-01-20"), resultat)
    }

    @Test
    fun `returnerer null når ingen tidligere vurdering finnes`() {
        val vurderinger = listOf(
            vurdering("2024-04-01", "2024-04-10"),
            vurdering("2024-04-11", "2024-04-20", Status.PAFOLGENDE)
        )

        val resultat = vurderinger.finnGrunnlagForFørstegangssøknaden(
            vurdering("2024-03-01", "2024-03-31", Status.PAFOLGENDE)
        )

        assertNull(resultat)
    }

    @Test
    fun `finner vurdering med samme medlemskapsperiode`() {
        val forventet = vurdering("2024-01-01", "2024-01-31")

        val resultat = listOf(
            vurdering("2024-02-01", "2024-02-29"),
            forventet
        ).finnMatchendeMedlemskapsperiode(
            request("2024-01-01", "2024-01-31")
        )

        assertEquals(forventet, resultat)
    }

    @Test
    fun `returnerer null når vurderingen ikke har samme periode`() {
        val resultat = listOf(
            vurdering("2024-01-01", "2024-01-30")
        ).finnMatchendeMedlemskapsperiode(
            request("2024-01-01", "2024-01-31")
        )

        assertNull(resultat)
    }

    private fun vurdering(
        fom: String,
        tom: String,
        status: Status = Status.JA
    ) = Vurderingsstatus(
        fnr = "12345678901",
        fom = LocalDate.parse(fom),
        tom = LocalDate.parse(tom),
        status = status
    )

    private fun request(fom: String, tom: String) =
        MedlemskapsstatusRequest(
            sykepengesoknad_id = "søknad-1",
            fnr = "12345678901",
            fom = LocalDate.parse(fom),
            tom = LocalDate.parse(tom)
        )
}
