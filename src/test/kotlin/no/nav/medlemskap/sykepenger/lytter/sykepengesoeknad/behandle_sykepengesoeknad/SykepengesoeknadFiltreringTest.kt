package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadsstatusDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO
import no.nav.medlemskap.sykepenger.lytter.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.persistence.BrukersporsmaalInMemmoryRepository
import no.nav.persistence.MedlemskapVurdertInMemmoryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SykepengesoeknadFiltreringTest {
    private val medlemskapRepository = MedlemskapVurdertInMemmoryRepository()
    private val persistenceService = PersistenceService(medlemskapRepository, BrukersporsmaalInMemmoryRepository())
    private val filtrering = SykepengesoeknadFiltrering(persistenceService)

    @Test
    fun `erDuplikat finner vurdering med samme fom og tom`() {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))

        val duplikat = filtrering.erDuplikat(
            Medlemskap(
                fnr = "12345678901",
                fom = LocalDate.of(2024, 1, 1),
                tom = LocalDate.of(2024, 1, 31),
                medlem = ErMedlem.UAVKLART
            )
        )

        assertNotNull(duplikat)
    }

    @Test
    fun `erDuplikat returnerer null når periode ikke finnes`() {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))

        val duplikat = filtrering.erDuplikat(
            Medlemskap(
                fnr = "12345678901",
                fom = LocalDate.of(2024, 2, 1),
                tom = LocalDate.of(2024, 2, 28),
                medlem = ErMedlem.UAVKLART
            )
        )

        assertNull(duplikat)
    }

    @Test
    fun `finnDuplikatSomSkalFiltreres filtrerer duplikat når arbeidUtenforNorge ikke er true`() {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))

        val skalFiltreres = filtrering.finnDuplikatSomSkalFiltreres(
            sykepengesøknad(
                fnr = "12345678901",
                fom = LocalDate.of(2024, 1, 1),
                tom = LocalDate.of(2024, 1, 31),
                arbeidUtenforNorge = false
            )
        )

        assertTrue(skalFiltreres)
    }

    @Test
    fun `finnDuplikatSomSkalFiltreres filtrerer ikke duplikat når arbeidUtenforNorge er true`() {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))

        val skalFiltreres = filtrering.finnDuplikatSomSkalFiltreres(
            sykepengesøknad(
                fnr = "12345678901",
                fom = LocalDate.of(2024, 1, 1),
                tom = LocalDate.of(2024, 1, 31),
                arbeidUtenforNorge = true
            )
        )

        assertFalse(skalFiltreres)
    }

    @Test
    fun `erPåfølgendeSøknad returnerer true når søknaden starter dagen etter eksisterende vurdering`() {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))

        val påfølgende = filtrering.erPåfølgendeSøknad(
            sykepengesøknad(
                fnr = "12345678901",
                fom = LocalDate.of(2024, 2, 1),
                tom = LocalDate.of(2024, 2, 28)
            )
        )

        assertTrue(påfølgende)
    }

    @Test
    fun `erPåfølgendeSøknad returnerer false for førstegangssøknad`() {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))

        val påfølgende = filtrering.erPåfølgendeSøknad(
            sykepengesøknad(
                fnr = "12345678901",
                fom = LocalDate.of(2024, 2, 1),
                tom = LocalDate.of(2024, 2, 28),
                førstegangssøknad = true
            )
        )

        assertFalse(påfølgende)
    }

    @Test
    fun `lagreHvisPåfølgendeSøknad lagrer påfølgende søknad med PAFOLGENDE status`() {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))
        val søknad = sykepengesøknad(
            fnr = "12345678901",
            fom = LocalDate.of(2024, 2, 1),
            tom = LocalDate.of(2024, 2, 28)
        )

        val lagret = filtrering.lagreHvisPåfølgendeSøknad(søknad)

        assertTrue(lagret)
        val lagretVurdering = medlemskapRepository.finnVurdering("12345678901")
            .single { it.id == søknad.id }
        assertEquals(ErMedlem.PAFOLGENDE.toString(), lagretVurdering.status)
        assertEquals(søknad.fom, lagretVurdering.fom)
        assertEquals(søknad.tom, lagretVurdering.tom)
    }

    @Test
    fun `lagreHvisPåfølgendeSøknad lagrer ikke når arbeidUtenforNorge er true`() {
        lagreVurdering(fnr = "12345678901", fom = LocalDate.of(2024, 1, 1), tom = LocalDate.of(2024, 1, 31))

        val lagret = filtrering.lagreHvisPåfølgendeSøknad(
            sykepengesøknad(
                fnr = "12345678901",
                fom = LocalDate.of(2024, 2, 1),
                tom = LocalDate.of(2024, 2, 28),
                arbeidUtenforNorge = true
            )
        )

        assertFalse(lagret)
        assertEquals(1, medlemskapRepository.finnVurdering("12345678901").size)
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
        tom: LocalDate,
        arbeidUtenforNorge: Boolean? = null,
        førstegangssøknad: Boolean? = null
    ): LovmeSoknadDTO =
        LovmeSoknadDTO(
            id = UUID.randomUUID().toString(),
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            status = SoknadsstatusDTO.SENDT.name,
            fnr = fnr,
            korrigerer = null,
            startSyketilfelle = fom,
            sendtNav = LocalDateTime.now(),
            fom = fom,
            tom = tom,
            arbeidUtenforNorge = arbeidUtenforNorge,
            forstegangssoknad = førstegangssøknad
        )
}
