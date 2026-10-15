package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.BehandleSykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadMelding
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.LagreBrukerspoersmaal
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SykepengesoeknadMottakTest {
    private val behandleSykepengesoknad = mockk<BehandleSykepengesoeknad>()
    private val lagreBrukersporsmaal = mockk<LagreBrukerspoersmaal>()
    private val mottak = SykepengesoeknadMottak(behandleSykepengesoknad, lagreBrukersporsmaal)

    @Test
    fun `parsefeil droppes uten lagring eller videre behandling`() = runBlocking {
        listOf("{", "{}", soknad("UKJENT_TYPE")).forEach { json ->
            mottak.behandle(melding(json))
        }

        verify { lagreBrukersporsmaal wasNot Called }
        verify { behandleSykepengesoknad wasNot Called }
    }

    @Test
    fun `fiskere filtreres uten lagring eller videre behandling`() = runBlocking {
        mottak.behandle(melding(soknad("FISKERE")))

        verify { lagreBrukersporsmaal wasNot Called }
        verify { behandleSykepengesoknad wasNot Called }
    }

    @Test
    fun `gyldig soknad behandles etter melding med parsefeil`() = runBlocking {
        every { lagreBrukersporsmaal.lagre(any()) } returns Unit
        coEvery { behandleSykepengesoknad.behandle(any()) } returns Unit

        mottak.behandle(melding("{"))
        mottak.behandle(melding(soknad("ARBEIDSTAKERE")))

        verify(exactly = 1) { lagreBrukersporsmaal.lagre(match { it.soknadid == "test-soknad" }) }
        coVerify(exactly = 1) {
            behandleSykepengesoknad.behandle(match { it.sykepengesøknadGrunnlag.id == "test-soknad" })
        }
    }

    private fun soknad(type: String) = """
        {
          "id": "test-soknad",
          "type": "$type",
          "status": "SENDT",
          "fnr": "test-person",
          "startSyketilfelle": "2024-01-01",
          "sendtNav": "2024-01-02T12:00:00",
          "fom": "2024-01-01",
          "tom": "2024-01-31",
          "ettersending": false,
          "sporsmal": []
        }
    """.trimIndent()

    private fun melding(json: String) = SykepengesoeknadMelding(
        partition = 0,
        offset = 1,
        value = json,
        key = "test-key",
        topic = "test-topic",
        timestamp = LocalDateTime.of(2024, 1, 2, 12, 0),
        timestampType = "CREATE_TIME"
    )
}
