package no.nav.medlemskap.sykepenger.lytter.speilvurdering.hent_vurdering

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_saga.MedlemskapSagaAPI
import no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus.Medlemskapsstatus
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.Periode
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.Speilsvar
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingMapper
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.Ytelse
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Speilvurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.opprett_vurdering.OpprettNyVurderingForSpeil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HentEllerOpprettVurderingTest {
    private val request = SpeilvurderingRequest(
        fnr = "12345678901",
        førsteDagForYtelse = LocalDate.parse("2024-01-01"),
        periode = Periode(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31")),
        ytelse = Ytelse.SYKEPENGER
    )

    @Test
    fun `returnerer vurdering fra Saga ved suksess`() = runBlocking {
        val sagaJson = """
            {
              "vurderingsID": "saga-vurdering-id",
              "datagrunnlag": { "fnr": "12345678901", "brukerinput": {} },
              "resultat": { "svar": "JA" },
              "kanal": "SPEIL"
            }
        """.trimIndent()
        val forventetSpeilVurdering = Speilvurdering(
            soknadId = "saga-vurdering-id",
            fnr = "12345678901",
            speilSvar = Speilsvar.JA,
            avklaringer = emptyList(),
            kanal = "SPEIL"
        )
        val opprett = mockk<OpprettNyVurderingForSpeil>()
        val medlemskapSagaApi = mockk<MedlemskapSagaAPI> {
            coEvery { finnVurdering(request, "call-id") } returns sagaJson
            coEvery { ping(any()) } returns "OK"
        }
        val service = HentEllerOpprettVurdering(
            medlemskapSagaService = MedlemskapSagaService(medlemskapSagaApi),
            opprettNyVurderingForSpeil = opprett,
            speilvurderingMapper = SpeilvurderingMapper()
        )

        val result = service.finnVurdering(request, "call-id")

        assertEquals(forventetSpeilVurdering, result)
        coVerify(exactly = 0) { opprett.opprett(any(), any()) }
    }

    @Test
    fun `oppretter ny vurdering når Saga svarer 404`() = runBlocking {
        val opprett = mockk<OpprettNyVurderingForSpeil>()
        val forventet = Speilvurdering("søknad-id", request.fnr, mockk(), emptyList(), "SPEIL")
        coEvery { opprett.opprett(request, "call-id") } returns forventet

        val result = hentMedSagaFeil(HttpStatusCode.NotFound, opprett).finnVurdering(request, "call-id")

        assertEquals(forventet, result)
        coVerify(exactly = 1) { opprett.opprett(request, "call-id") }
    }

    @Test
    fun `videresender andre HTTP-feil fra Saga`() = runBlocking {
        val opprett = mockk<OpprettNyVurderingForSpeil>(relaxed = true)

        assertThrows(ResponseException::class.java) {
            runBlocking {
                hentMedSagaFeil(HttpStatusCode.InternalServerError, opprett)
                    .finnVurdering(request, "call-id")
            }
        }

        coVerify(exactly = 0) { opprett.opprett(any(), any()) }
    }

    private fun hentMedSagaFeil(
        status: HttpStatusCode,
        opprett: OpprettNyVurderingForSpeil
    ): HentEllerOpprettVurdering {
        val response = mockk<HttpResponse> {
            every { this@mockk.status } returns status
        }
        val medlemskapSagaApi = mockk<MedlemskapSagaAPI> {
            coEvery { finnVurdering(any(), any()) } throws ResponseException(response, status.description)
            coEvery { hentMedlemskapsstatus(any(), any()) } returns mockk<Medlemskapsstatus>()
            coEvery { ping(any()) } returns "OK"
        }
        return HentEllerOpprettVurdering(
            medlemskapSagaService = MedlemskapSagaService(medlemskapSagaApi),
            opprettNyVurderingForSpeil = opprett,
            speilvurderingMapper = mockk<SpeilvurderingMapper>()
        )
    }
}
