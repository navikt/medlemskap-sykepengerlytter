package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Type
import no.nav.medlemskap.sykepenger.lytter.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Status
import no.nav.persistence.BrukersporsmaalInMemmoryRepository
import no.nav.persistence.MedlemskapVurdertInMemmoryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class BehandleSykepengesoeknadTest {
    private val medlemskapRepository = MedlemskapVurdertInMemmoryRepository()
    private val persistenceService = PersistenceService(medlemskapRepository, BrukersporsmaalInMemmoryRepository())
    private val lovmeApi = LovMeApiTestMock()
    private val behandleSykepengesoeknad = BehandleSykepengesoeknad(
        sykepengesoeknadFiltrering = SykepengesoeknadFiltrering(persistenceService),
        utledBrukerinput = UtledBrukerinput(persistenceService),
        lagreVurderingsstatus = LagreVurderingsstatus(persistenceService),
        medlemskapOppslagService = MedlemskapOppslagService(lovmeApi)
    )

    @Test
    fun `behandle sender søknad til LovMe og lagrer vurdering`() = runBlocking {
        val søknad = sykepengesøknad(
            fnr = "12345678901",
            fom = LocalDate.of(2019, 3, 22),
            tom = LocalDate.of(2019, 4, 8)
        )

        behandleSykepengesoeknad.behandle(søknad)

        assertEquals(1, lovmeApi.antallVurderMedlemskapKall)
        assertEquals("12345678901", lovmeApi.sisteRequest?.fnr)
        assertNotNull(lovmeApi.sisteRequest)
    }

    @Test
    fun `behandle sender ikke duplikat søknad til LovMe`() = runBlocking {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))
        val søknad = sykepengesøknad(
            fnr = "12345678901",
            fom = LocalDate.of(2024, 1, 1),
            tom = LocalDate.of(2024, 1, 31)
        )

        behandleSykepengesoeknad.behandle(søknad)
        assertEquals(0, lovmeApi.antallVurderMedlemskapKall)
        assertEquals(0, lovmeApi.antallVurderMedlemskapKall)
    }

    @Test
    fun `behandle lagrer påfølgende søknad uten å sende den til LovMe`() = runBlocking {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))
        val søknad = sykepengesøknad(
            fnr = "12345678901",
            fom = LocalDate.of(2024, 2, 1),
            tom = LocalDate.of(2024, 2, 28)
        )

        behandleSykepengesoeknad.behandle(søknad)

        assertEquals(0, lovmeApi.antallVurderMedlemskapKall)
    }

    private fun lagreVurdering(
        fnr: String,
        fom: LocalDate,
        tom: LocalDate,
        status: ErMedlem = ErMedlem.JA
    ) {
        medlemskapRepository.lagreVurdering(
            VurderingDao(
                id = UUID.randomUUID().toString(),
                fnr = fnr,
                fom = fom,
                tom = tom,
                status = status.toString()
            )
        )
    }

    private fun sykepengesøknad(
        fnr: String,
        fom: LocalDate,
        tom: LocalDate
    ): LovmeSoknadDTO =
        LovmeSoknadDTO(
            id = UUID.randomUUID().toString(),
            type = Type.ARBEIDSTAKERE,
            status = Status.SENDT.name,
            fnr = fnr,
            korrigerer = null,
            startSyketilfelle = fom,
            sendtNav = LocalDateTime.now(),
            fom = fom,
            tom = tom
        )

    private class LovMeApiTestMock : LovmeAPI {
        var antallVurderMedlemskapKall = 0
        var sisteRequest: MedlOppslagRequest? = null

        override suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
            antallVurderMedlemskapKall++
            sisteRequest = medlOppslagRequest
            return this::class.java.classLoader.getResource("sampleVurdering.json").readText(Charsets.UTF_8)
        }

        override suspend fun vurderMedlemskapBomlo(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
            error("Skal ikke kalle vurderMedlemskapBomlo")
        }

        override suspend fun brukerspørsmål(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
            error("Skal ikke kalle brukerspørsmål")
        }
    }
}
