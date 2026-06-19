package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadsstatusDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class MottakskriterierTest {

    @Test
    fun `skal behandle arbeidstakersøknad`() {
        val søknad = søknad(type = SoknadstypeDTO.ARBEIDSTAKERE)

        assertTrue(Mottakskriterier.erOppfylt(søknad))
    }

    @Test
    fun `skal behandle gradert reisetilskudd`() {
        val søknad = søknad(type = SoknadstypeDTO.GRADERT_REISETILSKUDD)

        assertTrue(Mottakskriterier.erOppfylt(søknad))
    }

    @Test
    fun `skal ikke behandle andre søknadstyper`() {
        val søknadstyperSomIkkeSkalBehandles = SoknadstypeDTO.entries
            .filterNot { it in setOf(SoknadstypeDTO.ARBEIDSTAKERE, SoknadstypeDTO.GRADERT_REISETILSKUDD) }

        søknadstyperSomIkkeSkalBehandles.forEach { søknadstype ->
            assertFalse(
                Mottakskriterier.erOppfylt(søknad(type = søknadstype)),
                "$søknadstype skal ikke behandles"
            )
        }
    }

    private fun søknad(type: SoknadstypeDTO) = LovmeSoknadDTO(
        id = "søknad-id",
        type = type,
        status = SoknadsstatusDTO.SENDT.name,
        fnr = "12345678910",
        startSyketilfelle = LocalDate.of(2024, 1, 1),
        sendtNav = LocalDateTime.of(2024, 1, 2, 12, 0),
        fom = LocalDate.of(2024, 1, 1),
        tom = LocalDate.of(2024, 1, 31)
    )
}
