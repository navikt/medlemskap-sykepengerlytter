package no.nav.medlemskap.sykepenger.lytter.service


import no.nav.medlemskap.sykepenger.lytter.persistence.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class MedlemskapBrukerSpormaalArbeidUtenforNorgeLogikkTest {

    @Test
    fun `Der bruker har oppgitt arbeid utland skal brukerspørsmål som er er eldre en 32 dager fjernes `(){
        val fnr = "12345678901"
        val arbeid1 = Medlemskap_utfort_arbeid_utenfor_norge(
            id="1",
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


        val b1 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now().minusDays(15),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),null,arbeid1
        )

        val funnet = finnMedlemskap_utfort_arbeid_utenfor_norge(listOf(b1))
        Assertions.assertNotNull(funnet,"Det skal finnes et releavant bruker spørsmål")
        Assertions.assertEquals("1",funnet!!.id,"ingen innslag slal filtreres ut i dette usecaset")
    }
    @Test
    fun `Der bruker har oppgitt flere arbeid utland og alle er aktuelle skal den nyeste returneres `(){
        val fnr = "12345678901"
        val arbeid1 = Medlemskap_utfort_arbeid_utenfor_norge(
            id="1",
            sporsmalstekst = "",
            svar = true,
            arbeidUtenforNorge = listOf(ArbeidUtenforNorge(
                id = "",
                arbeidsgiver = "NAV",
                land = "",
                perioder = listOf(Periode(LocalDate.of(2023,1,1), LocalDate.of(2023,1,31)))

            ))
        )
        val arbeid2 = Medlemskap_utfort_arbeid_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = true,
            arbeidUtenforNorge = listOf(ArbeidUtenforNorge(
                id = "",
                arbeidsgiver = "NAV",
                land = "",
                perioder = listOf(Periode(LocalDate.of(2023,1,1), LocalDate.of(2023,1,31)))

            ))
        )


        val b1 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now().minusDays(15),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),null,arbeid1
        )
        val b2 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now().minusDays(15),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),null,arbeid2
        )

        val funnet = finnMedlemskap_utfort_arbeid_utenfor_norge(listOf(b1,b2))
        Assertions.assertNotNull(funnet,"Det skal finnes et releavant bruker spørsmål")
        Assertions.assertEquals("2",funnet!!.id,"det nyeste brukerspørsmålet (sist registrerte skal returneres")
    }

    @Test
    fun `Dersom arbeid utland er registrert skal det prioriteres for ikke registrert arbeid selv om ingen arbeid er registrert etter `(){
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
            arbeidUtenforNorge = listOf(ArbeidUtenforNorge(
                id = "",
                arbeidsgiver = "NAV",
                land = "",
                perioder = listOf(Periode(LocalDate.of(2023,1,1), LocalDate.of(2023,1,31)))

            ))
        )


        val b1 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now().minusDays(10),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),null,arbeid1
        )
        val b2 = Brukersporsmaal("12345678901",UUID.randomUUID().toString(), LocalDate.now().minusDays(15),"SYKEPENGER","SENT",
            FlexBrukerSporsmaal(false),null,arbeid2
        )

        val funnet = finnMedlemskap_utfort_arbeid_utenfor_norge(listOf(b1,b2))
        Assertions.assertNotNull(funnet,"Det skal finnes et releavant bruker spørsmål")
        Assertions.assertEquals("2",funnet!!.id,"bruker spørsmålet med arbeid utland skal velges")
    }


}