package no.nav.medlemskap.sykepenger.lytter.service

import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.*
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
            "REGEL_5"
        )
        gyldigeRegler.forEach { regelbrudd ->
            {
                assertThat(
                    "Skal lage brukerspørsmål når det er ett regelbrudd",
                    genererBrukerspørsmål.skalGenerereBrukerSpørsmål(listOf(regelbrudd))
                )
                equals(true)
            }
        }

    }
    @Test
    fun skalLageBrukerspørsmålNårDetErEttRegelbruddPåEnMedlRegel() {
        val genererBrukerspørsmål = GenererBrukerSporsmaal()
        val gyldigeRegler = listOf(
            "REGEL_1_3_1", "REGEL_1_3_3", "REGEL_1_3_4", "REGEL_1_3_5"
        )
        gyldigeRegler.forEach { regelbrudd ->
            {
                assertThat(
                    "Skal lage brukerspørsmål når det er ett regelbrudd",
                    genererBrukerspørsmål.skalGenerereBrukerSpørsmål(listOf(regelbrudd))
                )
                equals(true)
            }
        }
    }
}