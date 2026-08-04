package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Type
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class InngangskriterierTest {

    @Test
    fun `er oppfylt for sendt arbeidstakersoknad som ikke er ettersending`() {
        val søknad = søknad(type = Type.ARBEIDSTAKERE)
        val resultat = Inngangskriterier.vurder(søknad)

        assertEquals(
            InngangskriterierResultat(erOppfylt = true, brutteKriterier = emptyList()),
            resultat
        )
    }

    @Test
    fun `er oppfylt for sendt gradert reisetilskudd som ikke er ettersending`() {
        val søknad = søknad(type = Type.GRADERT_REISETILSKUDD)

        assertTrue(Inngangskriterier.vurder(søknad).erOppfylt)
    }

    @Test
    fun `er ikke oppfylt nar soknaden ikke er sendt`() {
        val søknad = søknad(status = Status.NY.name)
        val resultat = Inngangskriterier.vurder(søknad)

        assertFalse(resultat.erOppfylt)
        assertEquals(
            listOf(BruttInngangskriterium.FEIL_STATUS),
            resultat.brutteKriterier
        )
    }

    @Test
    fun `er ikke oppfylt for soknadstyper som ikke skal behandles`() {
        val søknadstyperSomIkkeSkalBehandles = Type.entries
            .filterNot { it in setOf(Type.ARBEIDSTAKERE, Type.GRADERT_REISETILSKUDD) }

        søknadstyperSomIkkeSkalBehandles.forEach { søknadstype ->
            val resultat = Inngangskriterier.vurder(søknad(type = søknadstype))

            assertFalse(
                resultat.erOppfylt,
                "$søknadstype skal ikke behandles"
            )
            assertEquals(
                listOf(BruttInngangskriterium.IKKE_TILLATT_TYPE),
                resultat.brutteKriterier,
                "$søknadstype skal gi brutt kriterium"
            )
        }
    }

    @Test
    fun `er ikke oppfylt for ettersending`() {
        val søknad = søknad(ettersending = true)
        val resultat = Inngangskriterier.vurder(søknad)

        assertFalse(resultat.erOppfylt)
        assertEquals(
            listOf(BruttInngangskriterium.ER_ETTERSENDING),
            resultat.brutteKriterier
        )
    }

    @Test
    fun `vurder returnerer alle brutte kriterier`() {
        val søknad = søknad(
            type = Type.SELVSTENDIGE_OG_FRILANSERE,
            status = Status.NY.name,
            ettersending = true
        )

        assertEquals(
            InngangskriterierResultat(
                erOppfylt = false,
                brutteKriterier = listOf(
                    BruttInngangskriterium.FEIL_STATUS,
                    BruttInngangskriterium.IKKE_TILLATT_TYPE,
                    BruttInngangskriterium.ER_ETTERSENDING
                )
            ),
            Inngangskriterier.vurder(søknad)
        )
    }

    private fun søknad(
        type: Type = Type.ARBEIDSTAKERE,
        status: String = Status.SENDT.name,
        ettersending: Boolean = false,
    ) = SykepengesoeknadGrunnlag(
        id = "søknad-id",
        type = type,
        status = status,
        fnr = "12345678910",
        startSyketilfelle = LocalDate.of(2024, 1, 1),
        sendtNav = LocalDateTime.of(2024, 1, 2, 12, 0),
        fom = LocalDate.of(2024, 1, 1),
        tom = LocalDate.of(2024, 1, 31),
        ettersending = ettersending,
        sporsmal = JsonNodeFactory.instance.arrayNode()
    )
}
