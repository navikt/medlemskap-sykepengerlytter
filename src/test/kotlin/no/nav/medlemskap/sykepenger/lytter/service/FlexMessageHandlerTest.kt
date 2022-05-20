package no.nav.medlemskap.sykepenger.lytter.service

import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.FlexMessageRecord
import no.nav.medlemskap.sykepenger.lytter.domain.Soknadstatus
import no.nav.persistence.BrukersporsmaalInMemmoryRepository
import no.nav.persistence.MedlemskapVurdertInMemmoryRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class FlexMessageHandlerTest {
    @Test
    fun `test mapping av request Med Ja Svar i arbeidUtland`() = runBlocking {
        val key = UUID.randomUUID().toString()
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT_JA_SVAR.json").readText(Charsets.UTF_8)
        val record=FlexMessageRecord(1,1,fileContent,key,"test", LocalDateTime.now(),"timestampType")
        val service = FlexMessageHandler(Configuration(),PersistenceService(MedlemskapVurdertInMemmoryRepository(),BrukersporsmaalInMemmoryRepository()))
        val brukersporsmaal = service.mapMessage(record)
        assertNotNull(brukersporsmaal)
        assertNotNull(brukersporsmaal.sporsmaal)
        assertTrue(brukersporsmaal.sporsmaal!!.arbeidUtland!!)
        assertEquals("12454578474",brukersporsmaal.fnr,"fnr er ikke mappet korrekt")
        assertEquals(Soknadstatus.SENDT.toString(),brukersporsmaal.status,"status er ikke mappet korrekt")
        assertEquals(LocalDateTime.parse("2022-05-11T17:32:06.202609").toLocalDate(),brukersporsmaal.eventDate,"Korretkt dato er ikke valgt")
        assertEquals("52041604-a94a-38ca-b7a6-3e913b5207fa",brukersporsmaal.soknadid,"soknadID er ikke mappet korrekt")
        assertTrue(brukersporsmaal.sporsmaal!!.arbeidUtland!!,"arbeidUtland er ikke mappet korrekt")
    }
    @Test
    fun `test mapping av request Med Nei i ArbeidUtland`() = runBlocking {
        val key = UUID.randomUUID().toString()
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT.json").readText(Charsets.UTF_8)
        val record=FlexMessageRecord(1,1,fileContent,key,"test", LocalDateTime.now(),"timestampType")
        val service = FlexMessageHandler(Configuration(),PersistenceService(MedlemskapVurdertInMemmoryRepository(),BrukersporsmaalInMemmoryRepository()))
        val brukersporsmaal = service.mapMessage(record)
        assertNotNull(brukersporsmaal)
        assertNotNull(brukersporsmaal.sporsmaal)
        assertEquals("12454578474",brukersporsmaal.fnr,"fnr er ikke mappet korrekt")
        assertEquals(Soknadstatus.SENDT.toString(),brukersporsmaal.status,"status er ikke mappet korrekt")
        assertEquals(LocalDateTime.parse("2022-05-11T17:32:06.202609").toLocalDate(),brukersporsmaal.eventDate,"Korretkt dato er ikke valgt")
        assertEquals("52041604-a94a-38ca-b7a6-3e913b5207fa",brukersporsmaal.soknadid,"soknadID er ikke mappet korrekt")
        assertFalse(brukersporsmaal.sporsmaal!!.arbeidUtland!!,"arbeidUtland er ikke mappet korrekt")
    }
    @Test
    fun `test mapping av request UTEN ArbeidUtland`() = runBlocking {
        val key = UUID.randomUUID().toString()
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageUTEN_SPORSMAAL.json").readText(Charsets.UTF_8)
        val record=FlexMessageRecord(1,1,fileContent,key,"test", LocalDateTime.now(),"timestampType")
        val service = FlexMessageHandler(Configuration(),PersistenceService(MedlemskapVurdertInMemmoryRepository(),BrukersporsmaalInMemmoryRepository()))
        val brukersporsmaal = service.mapMessage(record)
        assertNotNull(brukersporsmaal)
        assertNotNull(brukersporsmaal.sporsmaal)
        assertEquals("28049120771",brukersporsmaal.fnr,"fnr er ikke mappet korrekt")
        assertEquals(Soknadstatus.SENDT.toString(),brukersporsmaal.status,"status er ikke mappet korrekt")
        assertEquals(LocalDateTime.parse("2021-08-18T08:04:18.99198").toLocalDate(),brukersporsmaal.eventDate,"Korretkt dato er ikke valgt")
        assertEquals("6743728c-815f-45dd-8b28-ff0bd1dbcf52",brukersporsmaal.soknadid,"soknadID er ikke mappet korrekt")
        assertNull(brukersporsmaal.sporsmaal!!.arbeidUtland,"Arbeid utland skal være null! ")
    }
    @Test
    fun `SENDT status skal lagres til database`() = runBlocking {
        val key = UUID.randomUUID().toString()
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT.json").readText(Charsets.UTF_8)
        val brukersporsmaalRepository = BrukersporsmaalInMemmoryRepository()
        val record=FlexMessageRecord(1,1,fileContent,key,"test", LocalDateTime.now(),"timestampType")
        val persistenceService = PersistenceService(MedlemskapVurdertInMemmoryRepository(),brukersporsmaalRepository)
        val service = FlexMessageHandler(Configuration(),persistenceService)
        service.handle(record)
        assertTrue(brukersporsmaalRepository.storage.size==1)
    }
    @Test
    fun `NY status skal ikke lagres til database`() = runBlocking {
        val key = UUID.randomUUID().toString()
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageNY.json").readText(Charsets.UTF_8)
        val brukersporsmaalRepository = BrukersporsmaalInMemmoryRepository()
        val record=FlexMessageRecord(1,1,fileContent,key,"test", LocalDateTime.now(),"timestampType")
        val persistenceService = PersistenceService(MedlemskapVurdertInMemmoryRepository(),brukersporsmaalRepository)
        val service = FlexMessageHandler(Configuration(),persistenceService)
        service.handle(record)
        assertTrue(brukersporsmaalRepository.storage.size==0)
    }
}
