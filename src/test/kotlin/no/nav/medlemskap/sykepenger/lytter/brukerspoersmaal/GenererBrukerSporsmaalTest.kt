package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.generer_foreslaatte_brukerspoersmaal.RegelbruddSomGirBrukerspoersmaal
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GenererBrukerSporsmaalTest {
    @Test
    fun skalIkkeLageBrukerspørsmålUtenRegelbrudd() {
        val genererBrukerspørsmål = RegelbruddSomGirBrukerspoersmaal()
        Assertions.assertFalse(genererBrukerspørsmål.skalGiBrukerspørsmål(emptyList()))
    }

    @Test
    fun skalLageBrukerspørsmålNårDetErEttRegelbruddPåEnEnkeltRegel() {
        val genererBrukerspørsmål = RegelbruddSomGirBrukerspoersmaal()
        val gyldigeRegler = listOf(
            "REGEL_3",
            "REGEL_19_3",
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
        org.assertj.core.api.Assertions.assertThat(gyldigeRegler).allSatisfy { regelbrudd ->
            org.assertj.core.api.Assertions.assertThat(
                genererBrukerspørsmål.skalGiBrukerspørsmål(
                    listOf(
                        regelbrudd
                    )
                )
            ).isEqualTo(true)
        }
    }


    @Test
    fun skalLageBrukerspørsmålNårDetErEttRegelbruddPåEnAv11Reglene() {
        val genererBrukerspørsmål = RegelbruddSomGirBrukerspoersmaal()
        val gyldigeRegler = listOf(
            "REGEL_11", "REGEL_11_2", "REGEL_11_2_1", "REGEL_11_3_1", "REGEL_11_3_1_1"
        )
        org.assertj.core.api.Assertions.assertThat(gyldigeRegler).allSatisfy { regelbrudd ->
            org.assertj.core.api.Assertions.assertThat(
                genererBrukerspørsmål.skalGiBrukerspørsmål(
                    listOf(
                        regelbrudd
                    )
                )
            ).isEqualTo(true)
        }
    }

    @Test
    fun skalIkkeLageBrukerspørsmålNårDetErUgyldigRegelbrudd() {
        val genererBrukerspørsmål = RegelbruddSomGirBrukerspoersmaal()
        val ugyldigeRegler = listOf(
            "REGEL_1", "REGEL_2", "REGEL_4", "REGEL_6", "REGEL_7", "REGEL_8", "REGEL_9", "REGEL_1_2", "REGEL_1_2_1"
        )
        org.assertj.core.api.Assertions.assertThat(ugyldigeRegler).allSatisfy { regelbrudd ->
            org.assertj.core.api.Assertions.assertThat(
                genererBrukerspørsmål.skalGiBrukerspørsmål(
                    listOf(
                        regelbrudd
                    )
                )
            ).isEqualTo(false)
        }

    }

    @Test
    fun skalLageBrukerspørsmålNårDetErFlerGyldigeRegelbrudd() {
        val genererBrukerspørsmål = RegelbruddSomGirBrukerspoersmaal()
        val flereGyldigeRegler = listOf("REGEL_3", "REGEL_15", "REGEL_20")
        val flereGyldigeRegler_2 = listOf("REGEL_1_3_1", "REGEL_1_3_3", "REGEL_1_3_4", "REGEL_1_3_5")

        org.assertj.core.api.Assertions.assertThat(genererBrukerspørsmål.skalGiBrukerspørsmål(flereGyldigeRegler))
            .isEqualTo(true)
        org.assertj.core.api.Assertions.assertThat(genererBrukerspørsmål.skalGiBrukerspørsmål(flereGyldigeRegler_2))
            .isEqualTo(true)

    }


    @Test
    fun skalLageBrukerspørsmålNårDetErFlerGyldigeRegelbruddFor11Reglene() {
        val genererBrukerspørsmål = RegelbruddSomGirBrukerspoersmaal()
        val flereGyldigeRegler = listOf("REGEL_11", "REGEL_11_2", "REGEL_11_2_1", "REGEL_11_3_1", "REGEL_11_3_1_1")
        org.assertj.core.api.Assertions.assertThat(genererBrukerspørsmål.skalGiBrukerspørsmål(flereGyldigeRegler))
            .isEqualTo(true)
    }

    @Test
    fun skalIkkeLageBrukerspørsmålNårGyldigOgUgyldigRegelKombineres() {
        val genererBrukerspørsmål = RegelbruddSomGirBrukerspoersmaal()
        val blandingAvRegler = listOf("REGEL_3", "REGEL_1", "REGEL_15", "REGEL_2")
        val blandingAvRegler_2 = listOf("REGEL_1_3_1", "REGEL_2")
        org.assertj.core.api.Assertions.assertThat(genererBrukerspørsmål.skalGiBrukerspørsmål(blandingAvRegler)).isEqualTo(false)
        org.assertj.core.api.Assertions.assertThat(genererBrukerspørsmål.skalGiBrukerspørsmål(blandingAvRegler_2))
            .isEqualTo(false)

    }


    @Test
    fun skalIkkeLageBrukerspørsmålPå11RegelogUgyldigRegel() {
        val genererBrukerspørsmål = RegelbruddSomGirBrukerspoersmaal()
        val blandingAvRegler = listOf("REGEL_11_2", "REGEL_2")
        org.assertj.core.api.Assertions.assertThat(genererBrukerspørsmål.skalGiBrukerspørsmål(blandingAvRegler)).isEqualTo(false)
    }

}