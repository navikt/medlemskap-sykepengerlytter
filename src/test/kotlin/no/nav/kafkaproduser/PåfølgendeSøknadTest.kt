package no.nav.kafkaproduser

import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.domain.PersonRecord
import no.nav.medlemskap.sykepenger.lytter.domain.erpåfølgende
import no.nav.medlemskap.sykepenger.lytter.jakson.JaksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.time.LocalDate

class PåfølgendeSøknadTest {

    @Test
    fun `JsonObjektInneholderFomTom`(){
        val json = JaksonParser().parseMedlemskap(Medlemskap("12343", LocalDate.now(), LocalDate.now(),"SYKEPENGER",ErMedlem.JA))
        println(json)
    }
    @Test
    fun `testParsePersonRecord`(){
        val sokndA = Medlemskap("1234", LocalDate.of(2022,1,1), LocalDate.of(2022,1,10),"SYK",ErMedlem.JA)
        val sokndB = Medlemskap("1234", LocalDate.of(2022,1,11), LocalDate.of(2022,1,20),"SYK",ErMedlem.JA)
        val personRecord:PersonRecord = PersonRecord("1234", arrayOf(sokndA,sokndB))
        val json = JaksonParser().parseRecord(personRecord)
        println(json)
    }
    @Test
    fun `test påfølgende logikk`(){

        val sokndA = Medlemskap("1234", LocalDate.of(2022,1,1), LocalDate.of(2022,1,10),"SYK",ErMedlem.JA)
        val sokndB = Medlemskap("1234", LocalDate.of(2022,1,11), LocalDate.of(2022,1,20),"SYK",ErMedlem.JA)
        println(sokndA)
        println(sokndB)
        Assertions.assertTrue(sokndB.erpåfølgende(sokndA))

    }
    @Test
    fun `test hash`(){
        val fnr = "21047541120"
        println(fnr.sha1)

    }
    val String.sha1: String
        get() {
            val bytes = MessageDigest.getInstance("SHA-256").digest(("lang stygg tekst som på ingen måte skal kunne autogenereres"+this).toByteArray())
            return bytes.joinToString("") {
                "%02x".format(it)
            }
        }
    @Test
    fun `test påfølgende logikk med medlemskap som starter midt i perioden`(){

        val sokndA = Medlemskap("1234", LocalDate.of(2022,1,1), LocalDate.of(2022,1,10),"SYK",ErMedlem.JA)
        val sokndB = Medlemskap("1234", LocalDate.of(2022,1,7), LocalDate.of(2022,1,20),"SYK",ErMedlem.JA)
        println(sokndA)
        println(sokndB)
        Assertions.assertTrue(sokndB.erpåfølgende(sokndA))

    }

    @Test
    fun `test påfølgende logikk med medlemskap som en dag for sent`(){

        val sokndA = Medlemskap("1234", LocalDate.of(2022,1,1), LocalDate.of(2022,1,10),"SYK",ErMedlem.JA)
        val sokndB = Medlemskap("1234", LocalDate.of(2022,1,12), LocalDate.of(2022,1,20),"SYK",ErMedlem.JA)
        println(sokndA)
        println(sokndB)
        Assertions.assertFalse(sokndB.erpåfølgende(sokndA))

    }
    @Test
    fun `test påfølgende logikk med medlemskap som er før`(){

        val sokndA = Medlemskap("1234", LocalDate.of(2022,1,10), LocalDate.of(2022,1,20),"SYK",ErMedlem.JA)
        val sokndB = Medlemskap("1234", LocalDate.of(2022,1,1), LocalDate.of(2022,1,9),"SYK",ErMedlem.JA)
        println(sokndA)
        println(sokndB)
        Assertions.assertFalse(sokndB.erpåfølgende(sokndA))

    }
}