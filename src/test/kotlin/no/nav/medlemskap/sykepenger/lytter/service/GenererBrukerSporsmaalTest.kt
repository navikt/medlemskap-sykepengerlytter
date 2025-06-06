package no.nav.medlemskap.sykepenger.lytter.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class GenererBrukerSporsmaalTest {
    @Test
    fun skalIkkeLageBrukerspørsmålUtenRegelbrudd() {
        val genererBrukerspørsmål = GenererBrukerSporsmaal()
        assertFalse(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(emptyList()))
    }

    @Test
    fun skalLageBrukerspørsmålNårDetErEttRegelbruddPåEnEnkeltRegel() {
        val genererBrukerspørsmål = GenererBrukerSporsmaal()
        val gyldigeRegler = listOf(
            "REGEL_3",
            "REGEL_19_3_1",
            "REGEL_15",
            "REGEL_C",
            "REGEL_12",
            "REGEL_20",
            "REGEL_34",
            "REGEL_21",
            "REGEL_25",
            "REGEL_10",
            "REGEL_5",
            "REGEL_1_3_1",
            "REGEL_1_3_3",
            "REGEL_1_3_4",
            "REGEL_1_3_5"
        )
        assertThat(gyldigeRegler).allSatisfy { regelbrudd ->
            assertThat(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(listOf(regelbrudd))).isEqualTo(true)
        }
    }


    @Test
    fun skalLageBrukerspørsmålNårDetErEttRegelbruddPåEnAv11Reglene() {
        val genererBrukerspørsmål = GenererBrukerSporsmaal()
        val gyldigeRegler = listOf(
            "REGEL_11", "REGEL_11_2", "REGEL_11_2_1", "REGEL_11_3_1", "REGEL_11_3_1_1"
        )
        assertThat(gyldigeRegler).allSatisfy { regelbrudd ->
            assertThat(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(listOf(regelbrudd))).isEqualTo(true)
        }
    }

    @Test
    fun skalIkkeLageBrukerspørsmålNårDetErUgyldigRegelbrudd() {
        val genererBrukerspørsmål = GenererBrukerSporsmaal()
        val ugyldigeRegler = listOf(
            "REGEL_1", "REGEL_2", "REGEL_4", "REGEL_6", "REGEL_7", "REGEL_8", "REGEL_9", "REGEL_1_2", "REGEL_1_2_1"
        )
        assertThat(ugyldigeRegler).allSatisfy { regelbrudd ->
            assertThat(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(listOf(regelbrudd))).isEqualTo(false)
        }

    }

    @Test
    fun skalLageBrukerspørsmålNårDetErFlerGyldigeRegelbrudd() {
        val genererBrukerspørsmål = GenererBrukerSporsmaal()
        val flereGyldigeRegler = listOf("REGEL_3", "REGEL_15", "REGEL_20")
        val flereGyldigeRegler_2 = listOf("REGEL_1_3_1", "REGEL_1_3_3", "REGEL_1_3_4", "REGEL_1_3_5")

        assertThat(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(flereGyldigeRegler)).isEqualTo(true)
        assertThat(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(flereGyldigeRegler_2)).isEqualTo(true)

    }


    @Test
    fun skalLageBrukerspørsmålNårDetErFlerGyldigeRegelbruddFor11Reglene() {
        val genererBrukerspørsmål = GenererBrukerSporsmaal()
        val flereGyldigeRegler = listOf("REGEL_11", "REGEL_11_2", "REGEL_11_2_1", "REGEL_11_3_1", "REGEL_11_3_1_1")
        assertThat(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(flereGyldigeRegler)).isEqualTo(true)
    }

    @Test
    fun skalIkkeLageBrukerspørsmålNårGyldigOgUgyldigRegelKombineres() {
        val genererBrukerspørsmål = GenererBrukerSporsmaal()
        val blandingAvRegler = listOf("REGEL_3", "REGEL_1", "REGEL_15", "REGEL_2")
        val blandingAvRegler_2 = listOf("REGEL_1_3_1", "REGEL_2")
        assertThat(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(blandingAvRegler)).isEqualTo(false)
        assertThat(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(blandingAvRegler_2)).isEqualTo(false)

    }


    @Test
    fun skalIkkeLageBrukerspørsmålPå11RegelogUgyldigRegel() {
        val genererBrukerspørsmål = GenererBrukerSporsmaal()
        val blandingAvRegler = listOf("REGEL_11_2", "REGEL_2")
        assertThat(genererBrukerspørsmål.skalGenerereBrukerSpørsmål(blandingAvRegler)).isEqualTo(false)
    }

}