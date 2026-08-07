package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.flexrespons

import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.MedlemskapVurderingMapper
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.generer_foreslaatte_brukerspoersmaal.ForeslaatteBrukerspoersmaalUtleder
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.flexrespons.tilFlexRespons
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.gjenbruk.finnSpørsmålSomSkalStilles
import no.nav.medlemskap.sykepenger.lytter.rest.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TilpassFlexResponsTest {

    private val medlemskapVurderingMapper = MedlemskapVurderingMapper()
    private val foreslaatteBrukerspoersmaalUtleder = ForeslaatteBrukerspoersmaalUtleder()

    @Test
    fun `legger til kjent oppholdstillatelse når spørsmålet skal stilles`() {
        val vurdering = medlemskapVurderingMapper.map(regelMotorRespons())

        val respons = foreslaatteBrukerspoersmaalUtleder
            .tilForeslåttBrukerspørsmål(vurdering)
            .finnSpørsmålSomSkalStilles(emptyList())
            .tilFlexRespons(vurdering)

        assertEquals(
            Periode(LocalDate.parse("2024-08-31"), LocalDate.parse("2025-08-31")),
            respons.kjentOppholdstillatelse
        )
    }

    @Test
    fun `utelater kjent oppholdstillatelse når spørsmålet kan gjenbrukes`() {
        val vurdering = medlemskapVurderingMapper.map(regelMotorRespons())
        val foreslåtteSpørsmål = foreslaatteBrukerspoersmaalUtleder.tilForeslåttBrukerspørsmål(vurdering)

        val respons = foreslåtteSpørsmål
            .finnSpørsmålSomSkalStilles(foreslåtteSpørsmål.toList())
            .tilFlexRespons(vurdering)

        assertNull(respons.kjentOppholdstillatelse)
    }

    private fun regelMotorRespons(): String =
        requireNotNull(
            this::class.java.classLoader.getResource("REGEL_19_3_har_ikke_oppholdstillatelse.json")
        ).readText(Charsets.UTF_8)
}
