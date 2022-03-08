package no.nav.medlemskap.sykepenger.lytter.service

import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.saga.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.persistence.InMemmoryRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class SoknadRecordHandlerTest {

    @Test
    fun `test Duplikat på forespørsel`() = runBlocking {
        val repo = InMemmoryRepository()
        val persistenceService = PersistenceService(repo)
        repo.lagreVurdering(VurderingDao(
            UUID.randomUUID().toString(),"123",
            LocalDate.of(2022,1,1),
            LocalDate.of(2022,1,10),
            ErMedlem.JA.toString())
        )
        repo.lagreVurdering(VurderingDao(
            UUID.randomUUID().toString(),"123",
            LocalDate.of(2022,1,11),
            LocalDate.of(2022,1,20),
            ErMedlem.JA.toString())
        )
        repo.lagreVurdering(VurderingDao(
            UUID.randomUUID().toString(),"234",
            LocalDate.of(2022,1,21),
            LocalDate.of(2022,1,25),
            ErMedlem.JA.toString())
        )
        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val duplikat = service.isDuplikat(
            Medlemskap(
                "123",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 10), ErMedlem.UAVKLART
            )
        )
        Assertions.assertNotNull(duplikat)
    }

    @Test
    fun `test ikke Duplikat på forespørsel`() = runBlocking {
        val repo = InMemmoryRepository()
        val persistenceService = PersistenceService(repo)
        repo.lagreVurdering(VurderingDao(
            UUID.randomUUID().toString(),"123",
            LocalDate.of(2022,1,1),
            LocalDate.of(2022,1,10),
            ErMedlem.JA.toString())
        )
        repo.lagreVurdering(VurderingDao(
            UUID.randomUUID().toString(),"123",
            LocalDate.of(2022,1,11),
            LocalDate.of(2022,1,20),
            ErMedlem.JA.toString())
        )
        repo.lagreVurdering(VurderingDao(
            UUID.randomUUID().toString(),"234",
            LocalDate.of(2022,1,21),
            LocalDate.of(2022,1,25),
            ErMedlem.JA.toString())
        )
        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val duplikat = service.isDuplikat(
            Medlemskap(
                "123",
                LocalDate.of(2022, 1, 21),
                LocalDate.of(2022, 1, 25), ErMedlem.UAVKLART
            )
        )
        Assertions.assertNull(duplikat)
    }

    @Test
    fun `test påfølgende forespørsel`() = runBlocking {
        val repo = InMemmoryRepository()
        val persistenceService = PersistenceService(repo)

        repo.lagreVurdering(VurderingDao(
            UUID.randomUUID().toString(),"01010112345",
            LocalDate.of(2021,8,25),
            LocalDate.of(2021,8,31),
            ErMedlem.JA.toString())
        )

        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        val paafolgende = service.isPaafolgendeSoknad(sykepengeSoknad)
        Assertions.assertTrue(paafolgende)
        val dbResult = repo.finnVurdering("01010112345")
        val paafolgendeMedlemskap = dbResult.find { it.status=="PAAFOLGENDE" }
        Assertions.assertNotNull(paafolgendeMedlemskap)
        Assertions.assertEquals(paafolgendeMedlemskap!!.fom,sykepengeSoknad.fom)
        Assertions.assertEquals(paafolgendeMedlemskap!!.tom,sykepengeSoknad.tom)
        Assertions.assertEquals(paafolgendeMedlemskap!!.fnr,sykepengeSoknad.fnr)
        Assertions.assertEquals(paafolgendeMedlemskap!!.id,sykepengeSoknad.id)
    }
    @Test
    fun `test påfølgende forespørsel via handle`() = runBlocking {
        val repo = InMemmoryRepository()
        val persistenceService = PersistenceService(repo)

        repo.lagreVurdering(VurderingDao(
            UUID.randomUUID().toString(),"01010112345",
            LocalDate.of(2021,8,25),
            LocalDate.of(2021,8,31),
            ErMedlem.JA.toString())
        )

        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        service.handle(SoknadRecord(1,1,"","","",sykepengeSoknad))
        val dbResult = repo.finnVurdering("01010112345")
        val paafolgendeMedlemskap = dbResult.find { it.status=="PAAFOLGENDE" }
        Assertions.assertNotNull(paafolgendeMedlemskap)
        Assertions.assertEquals(paafolgendeMedlemskap!!.fom,sykepengeSoknad.fom)
        Assertions.assertEquals(paafolgendeMedlemskap!!.tom,sykepengeSoknad.tom)
        Assertions.assertEquals(paafolgendeMedlemskap!!.fnr,sykepengeSoknad.fnr)
        Assertions.assertEquals(paafolgendeMedlemskap!!.id,sykepengeSoknad.id)
    }
    @Test
    fun `test duplikat forespørsel via handle`() = runBlocking {
        val repo = InMemmoryRepository()
        val persistenceService = PersistenceService(repo)

        repo.lagreVurdering(VurderingDao(
            UUID.randomUUID().toString(),"01010112345",
            LocalDate.of(2021,9,1),
            LocalDate.of(2021,9,30),
            ErMedlem.JA.toString())
        )

        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        service.handle(SoknadRecord(1,1,"","","",sykepengeSoknad))
        val dbResult = repo.finnVurdering("01010112345")
        Assertions.assertEquals(1,dbResult.size)

    }
}
