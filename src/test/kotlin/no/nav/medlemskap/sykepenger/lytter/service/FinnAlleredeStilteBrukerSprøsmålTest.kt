package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.persistence.*
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FinnAlleredeStilteBrukerSprøsmålTest {
    @Test
    fun `ingen bruker spørsmål skal føre til tom liste`(){
        val oppholdUtenforEOS = Medlemskap_opphold_utenfor_eos(
        id = "",
        sporsmalstekst ="",
        svar = false,
        oppholdUtenforEOS = emptyList()
        )
        val spormaal = finnAlleredeStilteBrukerSprøsmål(emptyList())
        Assertions.assertTrue(spormaal.isEmpty())
    }
    @Test
    fun `alle brukerspørsmaal gitt skal føre til komplett liste`(){

        val arbeidUtenForNorge = Medlemskap_utfort_arbeid_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = false,
            arbeidUtenforNorge = emptyList()
        )
        val oppholdUtenforEOS = Medlemskap_opphold_utenfor_eos(
            id = "",
            sporsmalstekst ="",
            svar = false,
            oppholdUtenforEOS = emptyList()
        )
        val oppholdUtenforNorge = Medlemskap_opphold_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = false,
            oppholdUtenforNorge = emptyList()
        )
        val oppholdsTilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id = "",
            sporsmalstekst = "",
            svar = true,
            vedtaksTypePermanent = true,
            vedtaksdato = LocalDate.now(),
            perioder = emptyList()
        )
        val brukersporsmaal = Brukersporsmaal(
            fnr="12345678901",
            eventDate = LocalDate.now(),
            soknadid = "",
            ytelse = "",
            status = "",
            sporsmaal = FlexBrukerSporsmaal(false),
            oppholdstilatelse = oppholdsTilatelse,
            utfort_arbeid_utenfor_norge = arbeidUtenForNorge,
            oppholdUtenforNorge = oppholdUtenforNorge,
            oppholdUtenforEOS = oppholdUtenforEOS
            )
        val spormaal = finnAlleredeStilteBrukerSprøsmål(listOf(brukersporsmaal))
        Assertions.assertFalse(spormaal.isEmpty())
        Assertions.assertTrue(spormaal.containsAll(listOf(
            Spørsmål.ARBEID_UTENFOR_NORGE,
            Spørsmål.OPPHOLDSTILATELSE,
            Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE,
            Spørsmål.OPPHOLD_UTENFOR_NORGE)))
    }
    @Test
    fun `noen brukerspørsmaal gitt skal føre en liste med bare de aktuelle sporsmaalene`(){

        val arbeidUtenForNorge = Medlemskap_utfort_arbeid_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = false,
            arbeidUtenforNorge = emptyList()
        )
        val oppholdUtenforEOS = Medlemskap_opphold_utenfor_eos(
            id = "",
            sporsmalstekst ="",
            svar = false,
            oppholdUtenforEOS = emptyList()
        )
        val oppholdUtenforNorge = Medlemskap_opphold_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = false,
            oppholdUtenforNorge = emptyList()
        )

        val brukersp1 = Brukersporsmaal(
            fnr="12345678901",
            eventDate = LocalDate.now().minusMonths(2),
            soknadid = "",
            ytelse = "",
            status = "",
            sporsmaal = FlexBrukerSporsmaal(false),
            oppholdUtenforNorge = oppholdUtenforNorge,
            oppholdUtenforEOS = oppholdUtenforEOS,
        )
        val brukersp2 = Brukersporsmaal(
            fnr="12345678901",
            eventDate = LocalDate.now().minusMonths(1),
            soknadid = "",
            ytelse = "",
            status = "",
            sporsmaal = FlexBrukerSporsmaal(false),
            utfort_arbeid_utenfor_norge = arbeidUtenForNorge,
            oppholdUtenforEOS = oppholdUtenforEOS
        )
        val brukersp3 = Brukersporsmaal(
            fnr="12345678901",
            eventDate = LocalDate.now(),
            soknadid = "",
            ytelse = "",
            status = "",
            sporsmaal = FlexBrukerSporsmaal(false),
            utfort_arbeid_utenfor_norge = arbeidUtenForNorge,
            oppholdUtenforNorge = oppholdUtenforNorge,
            oppholdUtenforEOS = oppholdUtenforEOS
        )
        val spormaal = finnAlleredeStilteBrukerSprøsmål(listOf(brukersp1,brukersp2,brukersp3))
        Assertions.assertFalse(spormaal.isEmpty())
        Assertions.assertTrue(spormaal.containsAll(listOf(
            Spørsmål.ARBEID_UTENFOR_NORGE,
            Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE,
            Spørsmål.OPPHOLD_UTENFOR_NORGE)))
    }
    @Test
    fun `flere resultater i bruker sprørsmall skal slå sammen og finne korrket kombinasjon`(){

        val arbeidUtenForNorge = Medlemskap_utfort_arbeid_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = false,
            arbeidUtenforNorge = emptyList()
        )
        val oppholdUtenforEOS = Medlemskap_opphold_utenfor_eos(
            id = "",
            sporsmalstekst ="",
            svar = false,
            oppholdUtenforEOS = emptyList()
        )
        val oppholdUtenforNorge = Medlemskap_opphold_utenfor_norge(
            id="2",
            sporsmalstekst = "",
            svar = false,
            oppholdUtenforNorge = emptyList()
        )
        val oppholdsTilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
            id = "",
            sporsmalstekst = "",
            svar = true,
            vedtaksTypePermanent = true,
            vedtaksdato = LocalDate.now(),
            perioder = emptyList()
        )
        val brukersp1 = Brukersporsmaal(
            fnr="12345678901",
            eventDate = LocalDate.now().minusMonths(2),
            soknadid = "",
            ytelse = "",
            status = "",
            sporsmaal = FlexBrukerSporsmaal(false),
            oppholdUtenforNorge = oppholdUtenforNorge,
            oppholdUtenforEOS = oppholdUtenforEOS,
            oppholdstilatelse = oppholdsTilatelse
        )
        val brukersp2 = Brukersporsmaal(
            fnr="12345678901",
            eventDate = LocalDate.now().minusMonths(1),
            soknadid = "",
            ytelse = "",
            status = "",
            sporsmaal = FlexBrukerSporsmaal(false),
            utfort_arbeid_utenfor_norge = arbeidUtenForNorge,
            oppholdUtenforEOS = oppholdUtenforEOS
        )
        val brukersp3 = Brukersporsmaal(
            fnr="12345678901",
            eventDate = LocalDate.now(),
            soknadid = "",
            ytelse = "",
            status = "",
            sporsmaal = FlexBrukerSporsmaal(false),
            utfort_arbeid_utenfor_norge = arbeidUtenForNorge,
            oppholdUtenforNorge = oppholdUtenforNorge,
            oppholdUtenforEOS = oppholdUtenforEOS
        )
        val spormaal = finnAlleredeStilteBrukerSprøsmål(listOf(brukersp1,brukersp2,brukersp3))
        Assertions.assertFalse(spormaal.isEmpty())
        Assertions.assertTrue(spormaal.containsAll(listOf(
            Spørsmål.ARBEID_UTENFOR_NORGE,
            Spørsmål.OPPHOLDSTILATELSE,
            Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE,
            Spørsmål.OPPHOLD_UTENFOR_NORGE)))
    }
}