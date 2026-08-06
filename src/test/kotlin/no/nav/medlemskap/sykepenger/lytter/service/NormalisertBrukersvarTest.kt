package no.nav.medlemskap.sykepenger.lytter.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NormalisertBrukersvarTest {
    @Test
    fun `gjenbruker svar uten oppholdstillatelse eller med oppholdstillatelse ja`() {
        val gjenbrukbareSvar = listOf(
            NormalisertBrukersvar(
                arbeidUtenforNorge = false,
                oppholdUtenforNorge = false,
                oppholdUtenforEos = null,
                oppholdstillatelse = null
            ),
            NormalisertBrukersvar(
                arbeidUtenforNorge = false,
                oppholdUtenforNorge = false,
                oppholdUtenforEos = null,
                oppholdstillatelse = true
            ),
            NormalisertBrukersvar(
                arbeidUtenforNorge = false,
                oppholdUtenforNorge = null,
                oppholdUtenforEos = false,
                oppholdstillatelse = null
            ),
            NormalisertBrukersvar(
                arbeidUtenforNorge = false,
                oppholdUtenforNorge = null,
                oppholdUtenforEos = false,
                oppholdstillatelse = true
            )
        )

        assertThat(gjenbrukbareSvar).allSatisfy { brukersvar ->
            assertThat(brukersvar.erGjenbrukbart()).isTrue()
        }
    }

    @Test
    fun `gjenbruker ikke svar med oppholdstillatelse nei`() {
        val ikkeGjenbrukbareSvar = listOf(
            NormalisertBrukersvar(
                arbeidUtenforNorge = false,
                oppholdUtenforNorge = false,
                oppholdUtenforEos = null,
                oppholdstillatelse = false
            ),
            NormalisertBrukersvar(
                arbeidUtenforNorge = false,
                oppholdUtenforNorge = null,
                oppholdUtenforEos = false,
                oppholdstillatelse = false
            )
        )

        assertThat(ikkeGjenbrukbareSvar).allSatisfy { brukersvar ->
            assertThat(brukersvar.erGjenbrukbart()).isFalse()
        }
    }
}
