package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar

import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadMelding
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class BrukersvarMapperTest {

    @Test
    fun `mapper bruker- og medlemskapssporsmal fra flexmelding`() {
        val brukersporsmaal = BrukersvarMapper.mapMessage(
            sykepengesoeknadMelding("FlexSampleMessageFlereBrukerSporsmaal.json")
        )

        assertEquals("51857200482", brukersporsmaal.fnr)
        assertEquals("4d6d35de-dc50-35ef-8f36-5cf59b2df922", brukersporsmaal.soknadid)
        assertEquals("SENDT", brukersporsmaal.status)
        assertEquals("SYKEPENGER", brukersporsmaal.ytelse)
        assertEquals(LocalDate.of(2023, 8, 23), brukersporsmaal.eventDate)

        assertNotNull(brukersporsmaal.sporsmaal)
        assertFalse(brukersporsmaal.sporsmaal!!.arbeidUtland!!)

        assertNull(brukersporsmaal.oppholdstilatelse)
    }

    @Test
    fun `mapper arbeid utenfor norge fra medlemskapssporsmal`() {
        val brukersporsmaal = BrukersvarMapper.mapMessage(
            sykepengesoeknadMelding("FlexSampleMessageFlereBrukerSporsmaal.json")
        )

        val arbeidUtenforNorge = brukersporsmaal.utfort_arbeid_utenfor_norge
        assertNotNull(arbeidUtenforNorge)
        assertEquals("694ee4e1-d6b9-306c-a940-bf87dad1665e", arbeidUtenforNorge!!.id)
        assertTrue(arbeidUtenforNorge.svar)
        assertEquals(2, arbeidUtenforNorge.arbeidUtenforNorge.size)

        val forsteArbeidsperiode = arbeidUtenforNorge.arbeidUtenforNorge.first()
        assertEquals("ad20699d-c443-3b3a-8821-759b0ce3d752", forsteArbeidsperiode.id)
        assertEquals("Arbeidsgiver", forsteArbeidsperiode.arbeidsgiver)
        assertEquals("Afghanistan", forsteArbeidsperiode.land)
        assertEquals(LocalDate.of(2023, 8, 1), forsteArbeidsperiode.perioder.single().fom)
        assertEquals(LocalDate.of(2023, 8, 24), forsteArbeidsperiode.perioder.single().tom)

        val andreArbeidsperiode = arbeidUtenforNorge.arbeidUtenforNorge.last()
        assertEquals("bff9cefd-d882-33c3-bbe4-03352f61dbc1", andreArbeidsperiode.id)
        assertEquals("To", andreArbeidsperiode.arbeidsgiver)
        assertEquals("Afghanistan", andreArbeidsperiode.land)
        assertEquals(LocalDate.of(2023, 8, 1), andreArbeidsperiode.perioder.single().fom)
        assertEquals(LocalDate.of(2023, 8, 18), andreArbeidsperiode.perioder.single().tom)
    }

    @Test
    fun `mapper opphold utenfor norge fra medlemskapssporsmal`() {
        val brukersporsmaal = BrukersvarMapper.mapMessage(
            sykepengesoeknadMelding("FlexSampleMessageFlereBrukerSporsmaal.json")
        )

        val oppholdUtenforNorge = brukersporsmaal.oppholdUtenforNorge
        assertNotNull(oppholdUtenforNorge)
        assertEquals("961cae09-3bae-3348-9c6d-ea042aceb3f0", oppholdUtenforNorge!!.id)
        assertTrue(oppholdUtenforNorge.svar)
        assertEquals(1, oppholdUtenforNorge.oppholdUtenforNorge.size)

        val opphold = oppholdUtenforNorge.oppholdUtenforNorge.single()
        assertEquals("f1a5f03e-0a30-3318-95a3-ec78116e2915", opphold.id)
        assertEquals("Bolivia", opphold.land)
        assertEquals("Forsørget medfølgende familiemedlem", opphold.grunn)
        assertEquals(LocalDate.of(2023, 8, 1), opphold.perioder.single().fom)
        assertEquals(LocalDate.of(2023, 8, 20), opphold.perioder.single().tom)
        assertNull(brukersporsmaal.oppholdUtenforEOS)
    }

    @Test
    fun `mapper opphold utenfor eos fra medlemskapssporsmal`() {
        val brukersporsmaal = BrukersvarMapper.mapMessage(
            sykepengesoeknadMelding("FlexSampleMessageFlereBrukerSporsmaal_EOS.json")
        )

        assertEquals("07867396440", brukersporsmaal.fnr)
        assertEquals("9b95c658-f6fc-319c-b8eb-ec5d27d99434", brukersporsmaal.soknadid)
        assertEquals(LocalDate.of(2023, 8, 23), brukersporsmaal.eventDate)
        assertNull(brukersporsmaal.oppholdstilatelse)
        assertNull(brukersporsmaal.oppholdUtenforNorge)

        val oppholdUtenforEOS = brukersporsmaal.oppholdUtenforEOS
        assertNotNull(oppholdUtenforEOS)
        assertEquals("4d1c7907-1571-3e43-b890-9f3498eb1f06", oppholdUtenforEOS!!.id)
        assertTrue(oppholdUtenforEOS.svar)
        assertEquals(1, oppholdUtenforEOS.oppholdUtenforEOS.size)

        val opphold = oppholdUtenforEOS.oppholdUtenforEOS.single()
        assertEquals("ae5d4266-007a-3b29-b919-21feb3739e8a", opphold.id)
        assertEquals("Frankrike", opphold.land)
        assertEquals("Studier", opphold.grunn)
        assertEquals(LocalDate.of(2023, 8, 15), opphold.perioder.single().fom)
        assertEquals(LocalDate.of(2023, 8, 23), opphold.perioder.single().tom)
    }

    private fun sykepengesoeknadMelding(resourceName: String): SykepengesoeknadMelding =
        SykepengesoeknadMelding(
            partition = 1,
            offset = 1,
            value = this::class.java.classLoader.getResource(resourceName).readText(Charsets.UTF_8),
            key = "test-key",
            topic = "test-topic",
            timestamp = LocalDateTime.now(),
            timestampType = "CreateTime"
        )
}
