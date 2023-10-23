package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.persistence.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BrukerinputMappingLogikkTest {

    @Test
    fun testmappingAvOppholdstilatelseMedNullVerdi(){
        val oppholdstilatelse = mapOppholdstilatelse(null)
        Assertions.assertNull(oppholdstilatelse)
    }
    @Test
    fun testmappingAvOppholdstilatelsePermanent(){
        val medlemskap_oppholdstilatelse_brukersporsmaal = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "abc",
            svar = true,
            vedtaksdato = LocalDate.now(),
            vedtaksTypePermanent = true,
            perioder = emptyList()
        )
        val oppholdstilatelse = mapOppholdstilatelse(medlemskap_oppholdstilatelse_brukersporsmaal)
        Assertions.assertEquals("1",oppholdstilatelse!!.id)
        Assertions.assertEquals("abc",oppholdstilatelse.sporsmalstekst)
        Assertions.assertEquals(true,oppholdstilatelse.svar)
        Assertions.assertEquals(true,oppholdstilatelse.vedtaksTypePermanent)
        Assertions.assertTrue(oppholdstilatelse.perioder.isEmpty())

    }
    @Test
    fun testmappingAvOppholdstilatelseMidlertidig(){
        val medlemskap_oppholdstilatelse_brukersporsmaal = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "abc",
            svar = true,
            vedtaksdato = LocalDate.now(),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.now(), LocalDate.now()))
        )
        val oppholdstilatelse = mapOppholdstilatelse(medlemskap_oppholdstilatelse_brukersporsmaal)
        Assertions.assertEquals("1",oppholdstilatelse!!.id)
        Assertions.assertEquals("abc",oppholdstilatelse.sporsmalstekst)
        Assertions.assertEquals(true,oppholdstilatelse.svar)
        Assertions.assertEquals(false,oppholdstilatelse.vedtaksTypePermanent)
        Assertions.assertEquals(LocalDate.now().toString(),oppholdstilatelse.perioder[0].fom)
        Assertions.assertEquals(LocalDate.now().toString(),oppholdstilatelse.perioder[0].tom)

    }
    @Test
    fun testmappingAvArbeidUtlandTrue(){
        val medlemskap_utfort_arbeid_utenfor_norge =
            Medlemskap_utfort_arbeid_utenfor_norge(
                id="1",
                sporsmalstekst = "abc",
                svar = true,
                arbeidUtenforNorge = listOf(
                    ArbeidUtenforNorge(
                        id = "2",
                        arbeidsgiver = "test",
                        land = "Sverige",
                        perioder = listOf(Periode(LocalDate.now(), LocalDate.now())))
                    ,
                    ArbeidUtenforNorge(
                        id = "3",
                        arbeidsgiver = "test2",
                        land = "Danmark",
                        perioder = listOf(Periode(LocalDate.now(), LocalDate.now())))
                )
            )
        val utfortAarbeidUtenforNorge = maputfortAarbeidUtenforNorge(medlemskap_utfort_arbeid_utenfor_norge)
        Assertions.assertNotNull(utfortAarbeidUtenforNorge,"dersom det finnes et innslag på arbeid utland så skal ser mappes")
        Assertions.assertEquals("1",utfortAarbeidUtenforNorge!!.id)
        Assertions.assertEquals("abc",utfortAarbeidUtenforNorge.sporsmalstekst)
        Assertions.assertEquals(true,utfortAarbeidUtenforNorge.svar)
        Assertions.assertTrue(utfortAarbeidUtenforNorge.arbeidUtenforNorge.isNotEmpty())
        Assertions.assertTrue(utfortAarbeidUtenforNorge.arbeidUtenforNorge.size == 2)
        Assertions.assertEquals("2",utfortAarbeidUtenforNorge.arbeidUtenforNorge[0].id)
        Assertions.assertEquals("Sverige",utfortAarbeidUtenforNorge.arbeidUtenforNorge[0].land)
        Assertions.assertEquals("test",utfortAarbeidUtenforNorge.arbeidUtenforNorge[0].arbeidsgiver)
        Assertions.assertEquals(LocalDate.now().toString(),utfortAarbeidUtenforNorge.arbeidUtenforNorge[0].perioder[0].fom)
        Assertions.assertEquals(LocalDate.now().toString(),utfortAarbeidUtenforNorge.arbeidUtenforNorge[0].perioder[0].tom)

    }
    @Test
    fun testmappingAvArbeidUtlandFalse(){
        val medlemskap_utfort_arbeid_utenfor_norge =
            Medlemskap_utfort_arbeid_utenfor_norge(
                id="1",
                sporsmalstekst = "abc",
                svar = false,
                arbeidUtenforNorge = listOf()
            )
        val utfortAarbeidUtenforNorge = maputfortAarbeidUtenforNorge(medlemskap_utfort_arbeid_utenfor_norge)
        Assertions.assertNotNull(utfortAarbeidUtenforNorge,"dersom det finnes et innslag på arbeid utland så skal ser mappes")
        Assertions.assertEquals("1",utfortAarbeidUtenforNorge!!.id)
        Assertions.assertEquals("abc",utfortAarbeidUtenforNorge.sporsmalstekst)
        Assertions.assertEquals(false,utfortAarbeidUtenforNorge.svar)
        Assertions.assertTrue(utfortAarbeidUtenforNorge.arbeidUtenforNorge.isEmpty())
    }
    @Test
    fun testmappingAvArbeidUtlandNullVerdi(){
        val medlemskap_utfort_arbeid_utenfor_norge = null
        val utfortAarbeidUtenforNorge = maputfortAarbeidUtenforNorge(medlemskap_utfort_arbeid_utenfor_norge)
        Assertions.assertNull(utfortAarbeidUtenforNorge)
    }
    @Test
    fun testmappingAvOppholdUtenforNorgeNullVerdi(){
        val medlemskapOppholdUtenforNorge = null
        val utfortAarbeidUtenforNorge = mapOppholdUtenforNorge(medlemskapOppholdUtenforNorge)
        Assertions.assertNull(utfortAarbeidUtenforNorge)
    }
    @Test
    fun testmappingAvOppholdUtenforNorgeFalse(){
        val medlemskapOppholdUtenforNorge = Medlemskap_opphold_utenfor_norge(
            id="1",
            sporsmalstekst = "abc",
            svar = false,
            oppholdUtenforNorge = emptyList()
        )
        val oppholdUtenforNorge = mapOppholdUtenforNorge(medlemskapOppholdUtenforNorge)
        Assertions.assertNotNull(oppholdUtenforNorge)
        Assertions.assertEquals("1",oppholdUtenforNorge!!.id)
        Assertions.assertEquals("abc",oppholdUtenforNorge.sporsmalstekst)
        Assertions.assertEquals(false,oppholdUtenforNorge.svar)
        Assertions.assertTrue(oppholdUtenforNorge.oppholdUtenforNorge.isEmpty())
    }

    @Test
    fun testmappingAvOppholdUtenforNorgeTrue(){
        val medlemskapOppholdUtenforNorge = Medlemskap_opphold_utenfor_norge(
            id="1",
            sporsmalstekst = "abc",
            svar = true,
            oppholdUtenforNorge = listOf(
                OppholdUtenforNorge(
                    id="2",
                    land = "Sverige",
                    grunn = "ingen",
                    perioder = listOf(Periode(LocalDate.now(), LocalDate.now()))
                ),
                OppholdUtenforNorge(
                    id="3",
                    land = "Danmark",
                    grunn = "ingen",
                    perioder = listOf(Periode(LocalDate.now(), LocalDate.now())))
                    )

        )

        val oppholdUtenforNorge = mapOppholdUtenforNorge(medlemskapOppholdUtenforNorge)
        Assertions.assertNotNull(oppholdUtenforNorge)
        Assertions.assertEquals("1",oppholdUtenforNorge!!.id)
        Assertions.assertEquals("abc",oppholdUtenforNorge.sporsmalstekst)
        Assertions.assertEquals(true,oppholdUtenforNorge.svar)
        Assertions.assertTrue(oppholdUtenforNorge.oppholdUtenforNorge.isNotEmpty())
        Assertions.assertTrue(oppholdUtenforNorge.oppholdUtenforNorge.size == 2)
        Assertions.assertEquals("2",oppholdUtenforNorge.oppholdUtenforNorge[0].id)
        Assertions.assertEquals("ingen",oppholdUtenforNorge.oppholdUtenforNorge[0].grunn)
        Assertions.assertEquals("Sverige",oppholdUtenforNorge.oppholdUtenforNorge[0].land)
        Assertions.assertEquals(LocalDate.now().toString(),oppholdUtenforNorge.oppholdUtenforNorge[0].perioder[0].fom)
        Assertions.assertEquals(LocalDate.now().toString(),oppholdUtenforNorge.oppholdUtenforNorge[0].perioder[0].tom)


    }
    @Test
    fun testmappingAvOppholdUtenforEOSFalse(){
        val medlemskapOppholdUtenforEOS = Medlemskap_opphold_utenfor_eos(
            id="1",
            sporsmalstekst = "abc",
            svar = false,
            oppholdUtenforEOS = emptyList()
        )
        val oppholdUtenforeos = mapOppholdUtenforEOS(medlemskapOppholdUtenforEOS)
        Assertions.assertNotNull(oppholdUtenforeos)
        Assertions.assertEquals("1",oppholdUtenforeos!!.id)
        Assertions.assertEquals("abc",oppholdUtenforeos.sporsmalstekst)
        Assertions.assertEquals(false,oppholdUtenforeos.svar)
        Assertions.assertTrue(oppholdUtenforeos.oppholdUtenforEOS.isEmpty())
    }
    @Test
    fun testmappingAvOppholdUtenforEOSTrue(){
        val medlemskapOppholdUtenforeos = Medlemskap_opphold_utenfor_eos(
            id="1",
            sporsmalstekst = "abc",
            svar = true,
            oppholdUtenforEOS = listOf(
                OppholdUtenforEOS(
                    id="2",
                    land = "Sverige",
                    grunn = "ingen",
                    perioder = listOf(Periode(LocalDate.now(), LocalDate.now()))
                ),
                OppholdUtenforEOS(
                    id="3",
                    land = "Danmark",
                    grunn = "ingen",
                    perioder = listOf(Periode(LocalDate.now(), LocalDate.now())))
            )

        )

        val oppholdUtenforeos = mapOppholdUtenforEOS(medlemskapOppholdUtenforeos)
        Assertions.assertNotNull(oppholdUtenforeos)
        Assertions.assertEquals("1",oppholdUtenforeos!!.id)
        Assertions.assertEquals("abc",oppholdUtenforeos.sporsmalstekst)
        Assertions.assertEquals(true,oppholdUtenforeos.svar)
        Assertions.assertTrue(oppholdUtenforeos.oppholdUtenforEOS.isNotEmpty())
        Assertions.assertTrue(oppholdUtenforeos.oppholdUtenforEOS.size == 2)
        Assertions.assertEquals("2",oppholdUtenforeos.oppholdUtenforEOS[0].id)
        Assertions.assertEquals("ingen",oppholdUtenforeos.oppholdUtenforEOS[0].grunn)
        Assertions.assertEquals("Sverige",oppholdUtenforeos.oppholdUtenforEOS[0].land)
        Assertions.assertEquals(LocalDate.now().toString(),oppholdUtenforeos.oppholdUtenforEOS[0].perioder[0].fom)
        Assertions.assertEquals(LocalDate.now().toString(),oppholdUtenforeos.oppholdUtenforEOS[0].perioder[0].tom)


    }


}