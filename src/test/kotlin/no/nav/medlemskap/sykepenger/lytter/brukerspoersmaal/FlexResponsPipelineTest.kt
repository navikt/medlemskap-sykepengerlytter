package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.rest.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FlexResponsPipelineTest {

    private val medlemskapVurderingMapper = MedlemskapVurderingMapper()
    private val regelMotorResponsHandler = RegelMotorResponsHandler()

    @Test
    fun `legger til kjent oppholdstillatelse når spørsmålet skal stilles`() {
        val vurdering = medlemskapVurderingMapper.map(regelMotorRespons())

        val respons = regelMotorResponsHandler
            .tilForeslåttFlexRespons(vurdering)
            .medSpørsmålSomSkalStilles(emptyList())
            .medKjentOppholdstillatelseFra(vurdering)

        assertEquals(
            Periode(LocalDate.parse("2024-08-31"), LocalDate.parse("2025-08-31")),
            respons.kjentOppholdstillatelse
        )
    }

    @Test
    fun `utelater kjent oppholdstillatelse når spørsmålet kan gjenbrukes`() {
        val vurdering = medlemskapVurderingMapper.map(regelMotorRespons())
        val foreslåttRespons = regelMotorResponsHandler.tilForeslåttFlexRespons(vurdering)

        val respons = foreslåttRespons
            .medSpørsmålSomSkalStilles(foreslåttRespons.sporsmal.toList())
            .medKjentOppholdstillatelseFra(vurdering)

        assertNull(respons.kjentOppholdstillatelse)
    }

    private fun regelMotorRespons(): String =
        requireNotNull(
            this::class.java.classLoader.getResource("REGEL_19_3_har_ikke_oppholdstillatelse.json")
        ).readText(Charsets.UTF_8)
}
