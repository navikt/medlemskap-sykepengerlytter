package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_oppholdstilatelse_brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class MedlemskapBrukerSpormaalLogikkTest {

    @Test
    fun `Flere Oppholdstilatelse skal finne den nyeste`(){
        val fnr = "12345678901"
        val opphold_1 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2000,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.MAX))
        )
        val opphold_2 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="2",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2010,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.MAX))
        )
        val opphold_3 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="3",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2020,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.MAX))
        )
        val b1 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_1
        )
        val b2 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_2
        )
        val b3 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_3
        )
        val funnet = finnMMedlemskap_oppholdstilatelse_brukersporsmaal(listOf(b1,b2,b3))
        Assertions.assertEquals("3",funnet!!.id,"siste oppholdstilatelse ble ikke funnet")
    }
    @Test
    fun `Flere Oppholdstilatelse skal finne permanent før løpende`(){
        val fnr = "12345678901"
        val opphold_1 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2000,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.MAX))
        )
        val opphold_2 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="2",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2010,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.MAX))
        )
        val opphold_3 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="3",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2020,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.MAX))
        )
        val peramanent = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="4",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2019,1,1),
            vedtaksTypePermanent = true,
            perioder = listOf())
        val b1 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_1
        )
        val b2 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_2
        )
        val b3 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_3
        )
        val b4 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),peramanent
        )
        val funnet = finnMMedlemskap_oppholdstilatelse_brukersporsmaal(listOf(b1,b2,b3,b4))
        Assertions.assertEquals("4",funnet!!.id,"oppholdatilatelse med permanent oppholdstilatelse skal finnes til fordel for midlertidig")
    }
    @Test
    fun `Flere Oppholdstilatelse der ingen fyller kriterene skal føre til null i svar`(){
        val fnr = "12345678901"
        val opphold_1 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="1",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2000,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.of(2010,1,1)))
        )
        val opphold_2 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="2",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2010,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.of(2020,1,1)))
        )
        val opphold_3 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="3",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2020,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.of(2023,1,1)))
        )

        val b1 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_1
        )
        val b2 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_2
        )
        val b3 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_3
        )

        val funnet = finnMMedlemskap_oppholdstilatelse_brukersporsmaal(listOf(b1,b2,b3))
        Assertions.assertNull(funnet,"oppholdatilatelse skal ikke finnes")
    }
    @Test
    fun `koden handterer null verdier`(){

        val opphold_2 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="2",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2010,1,1),
            vedtaksTypePermanent = false,
            perioder = emptyList()
        )
        val opphold_3 = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="3",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2020,1,1),
            vedtaksTypePermanent = false,
            perioder = listOf(Periode(LocalDate.MIN, LocalDate.MAX))
        )
        val peramanent = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id="4",
            sporsmalstekst = "",
            svar = true,
            vedtaksdato = LocalDate.of(2019,1,1),
            vedtaksTypePermanent = true,
            perioder = listOf())
        val b1 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),null
        )
        val b2 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_2
        )
        val b3 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),opphold_3
        )
        val b4 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now(),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),peramanent
        )
        val funnet = finnMMedlemskap_oppholdstilatelse_brukersporsmaal(listOf(b1,b2,b3,b4))
        Assertions.assertEquals("4",funnet!!.id,"oppholdatilatelse med permanent oppholdstilatelse skal finnes til fordel for midlertidig")
    }

}