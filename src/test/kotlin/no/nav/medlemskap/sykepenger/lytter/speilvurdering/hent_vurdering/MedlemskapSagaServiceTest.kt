package no.nav.medlemskap.sykepenger.lytter.speilvurdering.hent_vurdering

import com.fasterxml.jackson.core.JsonProcessingException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_saga.MedlemskapSagaAPI
import no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus.Medlemskapsstatus
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.Periode
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.Ytelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MedlemskapSagaServiceTest {
    private val request = SpeilvurderingRequest(
        fnr = "12345678901",
        førsteDagForYtelse = LocalDate.parse("2024-01-01"),
        periode = Periode(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31")),
        ytelse = Ytelse.SYKEPENGER
    )

    @Test
    fun `parser vanlig JSON-respons fra Saga`() = runBlocking {
        val sagaApi = fakeSagaApi(
            """
            {"datagrunnlag":{"fnr":"12345678901"},"resultat":{"svar":"JA"}}
            """.trimIndent()
        )

        val result = MedlemskapSagaService(sagaApi).finnVurdering(request, "call-id")

        assertEquals("12345678901", result.json.path("datagrunnlag").path("fnr").asText())
        assertEquals("JA", result.json.path("resultat").path("svar").asText())
    }

    @Test
    fun `parser JSON som er pakket inn som tekst`() = runBlocking {
        val sagaApi = fakeSagaApi(
            "\"{\\\"resultat\\\":{\\\"svar\\\":\\\"JA\\\"}}\""
        )

        val result = MedlemskapSagaService(sagaApi).finnVurdering(request, "call-id")

        assertEquals("JA", result.json.path("resultat").path("svar").asText())
    }

    @Test
    fun `kaster parsingfeil ved ugyldig JSON`() {
        val sagaApi = fakeSagaApi("Internal Server Error")

        assertThrows(JsonProcessingException::class.java) {
            runBlocking {
                MedlemskapSagaService(sagaApi).finnVurdering(request, "call-id")
            }
        }
    }

    private fun fakeSagaApi(response: String): MedlemskapSagaAPI = mockk {
        coEvery { finnVurdering(any(), any()) } returns response
        coEvery { hentMedlemskapsstatus(any(), any()) } returns mockk<Medlemskapsstatus>()
        coEvery { ping(any()) } returns "OK"
    }
}
