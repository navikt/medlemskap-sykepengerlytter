package no.nav.medlemskap.sykepenger.lytter.service


import com.natpryce.konfig.localDateType
import no.nav.medlemskap.sykepenger.lytter.persistence.*
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar
import org.apache.kafka.clients.admin.ListOffsetsOptions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class AlleredeStilteBrukerSpormaalTest {

    /*
    * Tester utført arbeid utland
    *  - siste registrerte bruker spørsmål (som inneholder utført arbeid utland)  skal alltid bli vurdert
    *  - JA i utført arbeid utland skal aldri føre til treff
    *  - NEI i arbeid utland skal finnes så lenge det er registrert for MINDRE en 32 dager siden
    * */

    @Test
    fun `Nor arbeid utland er registrert to ganger skal den nyeste vurderes`(){
        val fnr = "12345678901"
        val arbeid1 = Medlemskap_utfort_arbeid_utenfor_norge(
            id="1",
            sporsmalstekst = "",
            svar = false,
            arbeidUtenforNorge = emptyList()
        )
        val arbeid2 = Medlemskap_utfort_arbeid_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = true,
            arbeidUtenforNorge = listOf(
                ArbeidUtenforNorge(
                id = "",
                arbeidsgiver = "NAV",
                land = "THAILAND",
                perioder = listOf(Periode(LocalDate.of(2023,1,1), LocalDate.of(2023,1,31)))
                )
            )
        )
        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(10),arbeid1)
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(15),arbeid2)

        val funnet = finnAlleredeStilteBrukerSpørsmålArbeidUtland(listOf(b1,b2), LocalDate.now())
        Assertions.assertNotNull(funnet,"Det skal finnes et releavant bruker spørsmål")
        Assertions.assertEquals("1",funnet!!.id,"Nyeste bruker spørsmål skal alltid vurderes")
        Assertions.assertTrue(funnet.arbeidUtenforNorge.isEmpty())
        Assertions.assertFalse(funnet.svar)

    }
    @Test
    fun `Arbeid Utland=true skal aldri finnes som eksisterende bruker spørsmål`(){
        val fnr = "12345678901"

        val arbeid2 = Medlemskap_utfort_arbeid_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = true,
            arbeidUtenforNorge = listOf(
                ArbeidUtenforNorge(
                    id = "",
                    arbeidsgiver = "NAV",
                    land = "",
                    perioder = listOf(Periode(LocalDate.of(2023,1,1), LocalDate.of(2023,1,31)))
                )
            )
        )
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(15),arbeid2)

        val funnet = finnAlleredeStilteBrukerSpørsmålArbeidUtland(listOf(b2), LocalDate.now())
        Assertions.assertNull(funnet,"Arbeid utland sant skal ikke 'finnes'")


    }
    @Test
    fun `Arbeid Utland=false skal  finnes som eksisterende bruker spørsmål dersom alder på spørsmål er mindre en 32 dager`(){
        val fnr = "12345678901"

        val arbeid2 = Medlemskap_utfort_arbeid_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = false,
            arbeidUtenforNorge = emptyList()
            )
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(15),arbeid2)

        val funnet = finnAlleredeStilteBrukerSpørsmålArbeidUtland(listOf(b2), LocalDate.now())
        Assertions.assertNotNull(funnet,"Arbeid utland sant skal ikke 'finnes'")
        Assertions.assertEquals("2",funnet!!.id,"ukorrekt bruker spørsmål funnet")
        Assertions.assertTrue(funnet.arbeidUtenforNorge.isEmpty())
        Assertions.assertFalse(funnet.svar)
    }
    @Test
    fun `bruker sporsmaal uten arbeidUtenforNorge skal ikke paavire soek`(){
        val fnr = "12345678901"

        val arbeid2 = Medlemskap_utfort_arbeid_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = false,
            arbeidUtenforNorge = emptyList()
        )
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(15),arbeid2)
        val b3 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(1))

        val funnet = finnAlleredeStilteBrukerSpørsmålArbeidUtland(listOf(b2,b3), LocalDate.now())
        Assertions.assertNotNull(funnet,"Arbeid utland sant skal ikke 'finnes'")
        Assertions.assertEquals("2",funnet!!.id,"ukorrekt bruker spørsmål funnet")
        Assertions.assertTrue(funnet.arbeidUtenforNorge.isEmpty())
        Assertions.assertFalse(funnet.svar)
    }
    @Test
    fun `Arbeid Utland=false skal ikke finnes som eksisterende bruker sporsmaal dersom alder på sporsmaal er mer en 32 dager`(){
        val fnr = "12345678901"

        val arbeid2 = Medlemskap_utfort_arbeid_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = false,
            arbeidUtenforNorge = emptyList()
        )
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(32),arbeid2)

        val funnet = finnAlleredeStilteBrukerSpørsmålArbeidUtland(listOf(b2), LocalDate.now().plusDays(1))
        Assertions.assertNull(funnet,"Arbeid utland sant skal ikke 'finnes'")
    }


    /*
    * Tester opphold utenfor norge
    *  - siste registrerte bruker spørsmål (som inneholder opphhold utenfor norge)  skal alltid bli vurdert
    *  - JA i opphold utenfor norge skal aldri føre til treff
    *  - NEI i opphold utenfor norge skal finnes så lenge det er registrert for MINDRE en 5.5  mnd siden
    * */

    @Test
    fun `Nor Opphold utland utenfor norge er registrert to ganger skal den nyeste vurderes`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_opphold_utenfor_norge(
            id="1",
            sporsmalstekst = "",
            svar = false,
            oppholdUtenforNorge = emptyList()
        )
        val opphold2 = Medlemskap_opphold_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = true,
            oppholdUtenforNorge = listOf(
                OppholdUtenforNorge(
                    id = "",
                    land = "THAILAND",
                    grunn = "ferie",
                    perioder = listOf(Periode(LocalDate.of(2023,1,1), LocalDate.of(2023,1,31)))
                )
            )
        )
        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(10),opphold1)
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(15),opphold2)

        val funnet = finnAlleredeStilteBrukerSpørsmålOppholdUtenforNorge(listOf(b1,b2), LocalDate.now())
        Assertions.assertNotNull(funnet,"Det skal finnes et releavant bruker spørsmål")
        Assertions.assertEquals("1",funnet!!.id,"Nyeste bruker spørsmål skal alltid vurderes")
        Assertions.assertTrue(funnet.oppholdUtenforNorge.isEmpty())
        Assertions.assertFalse(funnet.svar)
    }

    @Test
    fun `Opphold utland =true skal ikke finnes om det dette er det sist registrete innslaget`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_opphold_utenfor_norge(
            id="1",
            sporsmalstekst = "",
            svar = true,
            oppholdUtenforNorge = listOf(
                OppholdUtenforNorge(
                    id="1",
                    land = "THAILAND",
                    grunn = "ferie",
                    perioder = listOf(Periode(LocalDate.MIN,LocalDate.MAX))
                )
            )
        )

        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(6),opphold1)

        val funnet = finnAlleredeStilteBrukerSpørsmålOppholdUtenforNorge(listOf(b1), LocalDate.now())
        Assertions.assertNull(funnet,"gamle spørsmål skal filtreres ut")
    }
    @Test
    fun `Opphold utland = false skal finnes om det er registrert for mindre en 32 dager siden`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_opphold_utenfor_norge(
            id="1",
            sporsmalstekst = "",
            svar = false,
            oppholdUtenforNorge = emptyList()
        )

        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().plusDays(10),opphold1)
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().plusDays(32))

        val funnet = finnAlleredeStilteBrukerSpørsmålOppholdUtenforNorge(listOf(b1,b2), LocalDate.now())
        Assertions.assertNotNull(funnet,"gamle spørsmål skal filtreres ut")
        Assertions.assertFalse(funnet!!.svar,"svar skal være false")
        Assertions.assertEquals("1",funnet!!.id,"Feil brukerspørmål funnet")
    }

    @Test
    fun `Opphold utland =false skal ikke finnes om det er registrert for mer en 5,5 mnd siden`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_opphold_utenfor_norge(
            id="1",
            sporsmalstekst = "",
            svar = false,
            oppholdUtenforNorge = emptyList()
        )

        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().minusMonths(6),opphold1)

        val funnet = finnAlleredeStilteBrukerSpørsmålOppholdUtenforNorge(listOf(b1), LocalDate.now())
        Assertions.assertNull(funnet,"gamle spørsmål skal filtreres ut")
    }



    /*
      * Tester opphold utenfor EØS
      *  - siste registrerte bruker spørsmål (som inneholder opphhold utenfor EØS)  skal alltid bli vurdert
      *  - JA i opphold utenfor EØS skal aldri føre til treff
      *  - NEI i opphold utenfor EØS skal finnes så lenge det er registrert for MINDRE en 5.5  mnd siden
      * */

    @Test
    fun `Nor Opphold utenfor EOS er registrert to ganger skal den nyeste vurderes`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_opphold_utenfor_eos(
            id="1",
            sporsmalstekst = "",
            svar = false,
            oppholdUtenforEOS = emptyList()
        )
        val opphold2 = Medlemskap_opphold_utenfor_eos(
            id="2",
            sporsmalstekst = "",
            svar = true,
            oppholdUtenforEOS = listOf(
                OppholdUtenforEOS(
                    id = "",
                    land = "THAILAND",
                    grunn = "ferie",
                    perioder = listOf(Periode(LocalDate.of(2023,1,1), LocalDate.of(2023,1,31)))
                )
            )
        )
        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(10),opphold1)
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(15),opphold2)

        val funnet = finnAlleredeStilteBrukerSpørsmålOppholdUtenforEOS(listOf(b1,b2), LocalDate.now())
        Assertions.assertNotNull(funnet,"Det skal finnes et releavant bruker spørsmål")
        Assertions.assertEquals("1",funnet!!.id,"Nyeste bruker spørsmål skal alltid vurderes")
        Assertions.assertTrue(funnet.oppholdUtenforEOS.isEmpty())
        Assertions.assertFalse(funnet.svar)
    }

    @Test
    fun `Opphold utenfor EOS =true skal ikke finnes om det dette er det sist registrete innslaget`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_opphold_utenfor_eos(
            id="1",
            sporsmalstekst = "",
            svar = true,
            oppholdUtenforEOS = listOf(
                OppholdUtenforEOS(
                    id="1",
                    land = "THAILAND",
                    grunn = "ferie",
                    perioder = listOf(Periode(LocalDate.MIN,LocalDate.MAX))
                )
            )
        )

        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(6),opphold1)

        val funnet = finnAlleredeStilteBrukerSpørsmålOppholdUtenforEOS(listOf(b1), LocalDate.now())
        Assertions.assertNull(funnet,"gamle spørsmål skal filtreres ut")
    }
    @Test
    fun `Opphold utenfor EOS = false skal finnes om det er registrert for mindre en 32 dager siden`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_opphold_utenfor_eos(
            id="1",
            sporsmalstekst = "",
            svar = false,
            oppholdUtenforEOS = emptyList()
        )

        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().plusDays(10),opphold1)
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().plusDays(32))

        val funnet = finnAlleredeStilteBrukerSpørsmålOppholdUtenforEOS(listOf(b1,b2), LocalDate.now())
        Assertions.assertNotNull(funnet,"gamle spørsmål skal filtreres ut")
        Assertions.assertFalse(funnet!!.svar,"svar skal være false")
        Assertions.assertEquals("1",funnet!!.id,"Feil brukerspørmål funnet")
    }

    @Test
    fun `Opphold utenfor EOS =false skal ikke finnes om det er registrert for mer en 5,5 mnd siden`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_opphold_utenfor_eos(
            id="1",
            sporsmalstekst = "",
            svar = false,
            oppholdUtenforEOS = emptyList()
        )

        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().minusMonths(6),opphold1)

        val funnet = finnAlleredeStilteBrukerSpørsmålOppholdUtenforEOS(listOf(b1), LocalDate.now())
        Assertions.assertNull(funnet,"gamle spørsmål skal filtreres ut")
    }

    /*
          * Tester oppholdstilatelse
          *  - siste registrerte bruker spørsmål (som inneholder opphholdstilateklse)  skal alltid bli vurdert
          *  - NEI i oppholdstilatelse skal aldri føre til treff
          *  - PERMANENT oppholdstilatelse skal finnes så lenge det er registrert for mindre en 1 år siden
          *  - MIDLERTIDIG oppholdstilatelse skal finnes så lenge perioden på oppgholdstilatelsen er gyldig
          * */

    @Test
    fun `Nor oppholdstilatelse er registrert to ganger skal den nyeste vurderes`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.now(),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.MAX))
        )
        val opphold2 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.now(),
            vedtaksTypePermanent = true,
            perioder = emptyList()
        )
        val b1 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(10),opphold1)
        val b2 = mockBrukerSpørsmål(fnr,LocalDate.now().minusDays(15),opphold2)

        val funnet = finnAlleredeStilteBrukerSpørsmåloppholdstilatelse(listOf(b1,b2), LocalDate.now())
        Assertions.assertNotNull(funnet,"Det skal finnes et releavant bruker spørsmål")
        Assertions.assertEquals("1",funnet!!.id,"Nyeste bruker spørsmål skal alltid vurderes")
        Assertions.assertTrue(funnet.svar)
        Assertions.assertFalse(funnet.vedtaksTypePermanent)
    }


    /*
    * Test av filtrerings logikk
    *   - Brukerspørsmål som er stilt tidligere (ref tester over) skal filtreres vekk fra foreslåtte bruker spørsmål
    * */
    @Test
    fun `allerede stile brukerspormaal skal filtreres ut fra foreslaat respons`(){
        val foreslaateSpørsmål = FlexRespons(Svar.UAVKLART, setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE))
        val allereredeStilteBrukerSpørsmål = listOf(Spørsmål.ARBEID_UTENFOR_NORGE)
        val actual_response = createFlexRespons(foreslaateSpørsmål,allereredeStilteBrukerSpørsmål)
        Assertions.assertEquals(Svar.UAVKLART,actual_response.svar)
        Assertions.assertTrue(actual_response.sporsmal.contains(Spørsmål.OPPHOLDSTILATELSE))
        Assertions.assertFalse(actual_response.sporsmal.contains(Spørsmål.ARBEID_UTENFOR_NORGE))
    }
    @Test
    fun `Oppholdstilatelse brukerspørsmål på gammel modell skal sorteres ut`(){
        val fnr = "12345678901"
        val opphold1 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "Har du oppholdstillatelse fra Utlendingsdirektoratet?",
            svar = true,
            vedtaksdato = LocalDate.now(),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.of(2024,1,1), LocalDate.now().plusDays(30)))
        )
        val opphold2 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "Har Utlendingsdirektoratet gitt deg en oppholdstillatelse før 1. januar 2024?",
            svar = true,
            vedtaksdato = LocalDate.now(),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.of(2022,1,1), LocalDate.of(2023,12,31)))
        )
        val sp1 = Brukersporsmaal(
            soknadid = UUID.randomUUID().toString(),
            eventDate = LocalDate.of(2023,4,23),
            fnr = fnr,
            sporsmaal = FlexBrukerSporsmaal(false),
            status = "",
            ytelse = "SYKEPENGER",
            oppholdstilatelse = opphold1
        )
        val sp2 = Brukersporsmaal(
            soknadid = UUID.randomUUID().toString(),
            eventDate = LocalDate.of(2024,4,24),
            fnr = fnr,
            sporsmaal = FlexBrukerSporsmaal(false),
            status = "",
            ytelse = "SYKEPENGER",
            oppholdstilatelse = opphold2
        )
        val response = finnAlleredeStilteBrukerSpørsmåloppholdstilatelse(listOf(sp1), LocalDate.now())
        Assertions.assertNull(response,"Skal ikke funne spørsmål på gammel modell")
    }

    @Test

    fun `Oppholdstilatelse brukerspørsmål på ny modell skal ikke sorteres ut`(){
        val opphold2 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "Har Utlendingsdirektoratet gitt deg en oppholdstillatelse før 1. januar 2024?",
            svar = true,
            vedtaksdato = LocalDate.now(),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.of(2022,1,1), LocalDate.now().plusDays(1)))
        )
        val sp2 = Brukersporsmaal(
            soknadid = UUID.randomUUID().toString(),
            eventDate = LocalDate.of(2024,4,24),
            fnr = "12345678910",
            sporsmaal = FlexBrukerSporsmaal(false),
            status = "",
            ytelse = "SYKEPENGER",
            oppholdstilatelse = opphold2

        )
        val res = finnAlleredeStilteBrukerSpørsmåloppholdstilatelse(listOf(sp2), LocalDate.now())
        Assertions.assertNotNull(res, "Nye brukerspørsmål er sortert ut")
    }

    fun mockBrukerSpørsmål(fnr:String, eventDate:LocalDate, arbeidUtenforNorge:Medlemskap_utfort_arbeid_utenfor_norge):Brukersporsmaal{
        return Brukersporsmaal(
            fnr=fnr,
            soknadid = UUID.randomUUID().toString(),
            eventDate = eventDate,
            ytelse="SYKEPENGER",
            status= "SENT",
            sporsmaal = FlexBrukerSporsmaal(false),
            oppholdstilatelse = null,
            utfort_arbeid_utenfor_norge = arbeidUtenforNorge)
    }
    fun mockBrukerSpørsmål(fnr:String, eventDate:LocalDate, oppholdstilatelse:Medlemskap_oppholdstilatelse_brukersporsmaal):Brukersporsmaal{
        return Brukersporsmaal(
            fnr=fnr,
            soknadid = UUID.randomUUID().toString(),
            eventDate = eventDate,
            ytelse="SYKEPENGER",
            status= "SENT",
            sporsmaal = FlexBrukerSporsmaal(false),
            oppholdstilatelse = oppholdstilatelse)
    }
    fun mockBrukerSpørsmål(fnr:String, eventDate:LocalDate, oppholdUtenforNorge:Medlemskap_opphold_utenfor_norge):Brukersporsmaal{
        return Brukersporsmaal(
            fnr=fnr,
            soknadid = UUID.randomUUID().toString(),
            eventDate = eventDate,
            ytelse="SYKEPENGER",
            status= "SENT",
            sporsmaal = FlexBrukerSporsmaal(false),
            oppholdstilatelse = null,
            oppholdUtenforNorge = oppholdUtenforNorge)
    }
    fun mockBrukerSpørsmål(fnr:String, eventDate:LocalDate, oppholdUtenforEOS:Medlemskap_opphold_utenfor_eos):Brukersporsmaal{
        return Brukersporsmaal(
            fnr=fnr,
            soknadid = UUID.randomUUID().toString(),
            eventDate = eventDate,
            ytelse="SYKEPENGER",
            status= "SENT",
            sporsmaal = FlexBrukerSporsmaal(false),
            oppholdstilatelse = null,
            oppholdUtenforEOS = oppholdUtenforEOS)
    }
    fun mockBrukerSpørsmål(fnr:String, eventDate:LocalDate):Brukersporsmaal{
        return Brukersporsmaal(
            fnr=fnr,
            soknadid = UUID.randomUUID().toString(),
            eventDate = eventDate,
            ytelse="SYKEPENGER",
            status= "SENT",
            sporsmaal = FlexBrukerSporsmaal(false),
            oppholdstilatelse = null,
            oppholdUtenforNorge = null)
    }

}