package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadsstatusDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class RegelmotorInngangskriterierTest {

    @Test
    fun `er oppfylt for sendt arbeidstakersøknad som ikke er ettersending`() {
        val søknad = søknad(type = SoknadstypeDTO.ARBEIDSTAKERE)

        assertTrue(RegelmotorInngangskriterier.erOppfylt(søknad))
    }

    @Test
    fun `er oppfylt for sendt gradert reisetilskudd som ikke er ettersending`() {
        val søknad = søknad(type = SoknadstypeDTO.GRADERT_REISETILSKUDD)

        assertTrue(RegelmotorInngangskriterier.erOppfylt(søknad))
    }

    @Test
    fun `er ikke oppfylt når søknaden ikke er sendt`() {
        val søknad = søknad(status = SoknadsstatusDTO.NY.name)

        assertFalse(RegelmotorInngangskriterier.erOppfylt(søknad))
    }

    @Test
    fun `er ikke oppfylt for søknadstype som ikke skal vurderes av regelmotor`() {
        val søknad = søknad(type = SoknadstypeDTO.SELVSTENDIGE_OG_FRILANSERE)

        assertFalse(RegelmotorInngangskriterier.erOppfylt(søknad))
    }

    @Test
    fun `er ikke oppfylt for ettersending`() {
        val søknad = søknad(ettersending = true)

        assertFalse(RegelmotorInngangskriterier.erOppfylt(søknad))
    }

    private fun søknad(
        type: SoknadstypeDTO = SoknadstypeDTO.ARBEIDSTAKERE,
        status: String = SoknadsstatusDTO.SENDT.name,
        ettersending: Boolean = false,
    ) = LovmeSoknadDTO(
        id = "søknad-id",
        type = type,
        status = status,
        fnr = "12345678910",
        startSyketilfelle = LocalDate.of(2024, 1, 1),
        sendtNav = LocalDateTime.of(2024, 1, 2, 12, 0),
        fom = LocalDate.of(2024, 1, 1),
        tom = LocalDate.of(2024, 1, 31),
        ettersending = ettersending
    )
}
