package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_saga.MedlemskapSagaAPI
import no.nav.medlemskap.sykepenger.lytter.domain.Status as VurderingsstatusStatus
import no.nav.medlemskap.sykepenger.lytter.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest
import no.nav.persistence.BrukersporsmaalInMemmoryRepository
import no.nav.persistence.MedlemskapVurdertInMemmoryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class FinnMedlemskapsstatusTest {
    private val vurderingsstatusRepository = MedlemskapVurdertInMemmoryRepository()
    private val saga = FakeMedlemskapSaga()
    private val finnMedlemskapsstatus = FinnMedlemskapsstatus(
        persistenceService = PersistenceService(
            vurderingsstatusRepository,
            BrukersporsmaalInMemmoryRepository()
        ),
        medlemskapsstatusService = MedlemskapsstatusService(saga)
    )

    @Test
    fun `vurdering som ikke er påfølgende bruker original vurderingsperiode`() = runBlocking {
        val request = request("2024-03-01", "2024-03-31")
        lagreVurdering(request.fom, request.tom, VurderingsstatusStatus.JA)
        lagreVurdering(
            LocalDate.parse("2024-01-01"),
            LocalDate.parse("2024-01-31"),
            VurderingsstatusStatus.JA
        )

        finnMedlemskapsstatus.finnMedlemskapsstatus(request, "call-id")

        assertEquals(request, saga.sisteRequest)
    }

    @Test
    fun `påfølgende vurdering bruker siste tidligere vurderingsperiode`() = runBlocking {
        lagreVurdering(LocalDate.parse("2024-01-01"), LocalDate.parse("2024-01-31"))
        lagreVurdering(LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-29"))
        val request = request("2024-03-01", "2024-03-31")
        lagreVurdering(request.fom, request.tom, VurderingsstatusStatus.PAFOLGENDE)

        finnMedlemskapsstatus.finnMedlemskapsstatus(request, "call-id")

        assertEquals(
            request.copy(
                fom = LocalDate.parse("2024-02-01"),
                tom = LocalDate.parse("2024-02-29")
            ),
            saga.sisteRequest
        )
    }

    @Test
    fun `påfølgende vurdering uten tidligere vurdering returnerer null og kaller ikke Saga`() = runBlocking {
        val request = request("2024-03-01", "2024-03-31")
        lagreVurdering(request.fom, request.tom, VurderingsstatusStatus.PAFOLGENDE)

        val resultat = finnMedlemskapsstatus.finnMedlemskapsstatus(request, "call-id")

        assertNull(resultat)
        assertNull(saga.sisteRequest)
    }

    @Test
    fun `ingen matchende vurdering sender original request til Saga`() = runBlocking {
        val request = request("2024-03-01", "2024-03-31")
        lagreVurdering(LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-29"))

        finnMedlemskapsstatus.finnMedlemskapsstatus(request, "call-id")

        assertEquals(request, saga.sisteRequest)
    }


    private fun lagreVurdering(
        fom: LocalDate,
        tom: LocalDate,
        status: VurderingsstatusStatus = VurderingsstatusStatus.JA
    ) {
        vurderingsstatusRepository.lagreVurdering(
            VurderingDao(
                id = UUID.randomUUID().toString(),
                fnr = "12345678901",
                fom = fom,
                tom = tom,
                status = status.toString()
            )
        )
    }

    private fun request(fom: String, tom: String) =
        MedlemskapsstatusRequest(
            sykepengesoknad_id = "søknad-1",
            fnr = "12345678901",
            fom = LocalDate.parse(fom),
            tom = LocalDate.parse(tom)
        )

    private class FakeMedlemskapSaga : MedlemskapSagaAPI {
        val response = Medlemskapsstatus(
            sykepengesoknad_id = "søknad-1",
            vurdering_id = "vurdering-1",
            fnr = "12345678901",
            fom = LocalDate.parse("2024-01-01"),
            tom = LocalDate.parse("2024-01-31"),
            status = Status.JA
        )
        var sisteRequest: MedlemskapsstatusRequest? = null
        var sisteCallId: String? = null
        var exception: Exception? = null

        override suspend fun hentMedlemskapsstatus(
            medlemskapsstatusRequest: MedlemskapsstatusRequest,
            callId: String
        ): Medlemskapsstatus {
            exception?.let { throw it }
            sisteRequest = medlemskapsstatusRequest
            sisteCallId = callId
            return response
        }

        override suspend fun finnVurdering(
            speilvurderingRequest: SpeilvurderingRequest,
            callId: String
        ): String = error("Ikke relevant for denne testen")

        override suspend fun ping(callId: String): String =
            error("Ikke relevant for denne testen")
    }
}
