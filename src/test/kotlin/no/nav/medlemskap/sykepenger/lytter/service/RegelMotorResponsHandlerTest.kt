package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RegelMotorResponsHandlerTest {
    @Test
    fun eosborger() {
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_EOS_BORGER.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.erEosBorger())
        Assertions.assertFalse(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertNull(jsonNode.oppholdsTillatelsePeriode())


    }

    @Test
    fun tredjelandsBorgerGiftMedEOSPerson() {
        val fileContent =
            this::class.java.classLoader.getResource("UAVKLART_3LAND_GIFTEOS_BORGER.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertTrue(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }

    @Test
    fun tredjelandsBorgerGiftMedEOSPersonSomHarOppholdsTilatelse() {
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_3LAND_GIFTEOS_BORGER_MED_OPPHOLD.json")
            .readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertTrue(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertTrue(jsonNode.harOppholdsTilatelse())
    }

    @Test
    fun kanHenteUtPeriodeFraOppholdsTilatelse() {
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_3LAND_GIFTEOS_BORGER_MED_OPPHOLD.json")
            .readText(Charsets.UTF_8)
        val jsonNode = objectMapper.readTree(fileContent)
        val periode = jsonNode.oppholdsTillatelsePeriode()
        Assertions.assertNotNull(periode)

    }

    @Test
    fun tredjelandsBorgerIkkeGiftMedEOSPerson() {
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_3LAND_IKKE_GIFT_EOS_BORGER.json")
            .readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }

    @Test
    fun regel_19_1() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_1.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }


    fun regel_19_3_1() {
        val fileContent = this::class.java.classLoader.getResource("respons_regelmotor_kunn_19_3_1_brudd.json")
            .readText(Charsets.UTF_8)
        val anbefalt = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
        val periode = RegelMotorResponsHandler().hentOppholdsTilatelsePeriode(fileContent)
        Assertions.assertNotNull(periode)
        Assertions.assertNotNull(periode!!.fom)
        Assertions.assertNotNull(periode!!.tom)
        Assertions.assertTrue(
            anbefalt.sporsmal.containsAll(
                setOf(
                    Spørsmål.OPPHOLDSTILATELSE,
                    Spørsmål.ARBEID_UTENFOR_NORGE,
                    Spørsmål.OPPHOLD_UTENFOR_NORGE
                )
            ), "oppholdstilatelse, arbeid utenfor norge og opphold utenfor norge skal finnes"
        )
    }

    @Test
    fun regel_19_3_1_medPermanentOpphold() {
        val fileContent =
            this::class.java.classLoader.getResource("REGEL_19_3_1_med_Permanent.json").readText(Charsets.UTF_8)
        val anbefalt = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        val periode = RegelMotorResponsHandler().hentOppholdsTilatelsePeriode(fileContent)
        Assertions.assertNotNull(periode)
        Assertions.assertNotNull(periode!!.fom)
        Assertions.assertNull(periode!!.tom)
        // Assertions.assertTrue(anbefalt.sporsmal.containsAll(setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE)),"oppholdstilatelse, arbeid utenfor norge og opphold utenfor norge skal finnes")
    }

    @Test
    fun regel_19_8() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_8.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }

    @Test
    fun regel_19_7() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_7.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.erBritiskBorger())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }

    @Test
    fun REGEL_0_1() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_0_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erBritiskBorger())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }

    @Test
    fun aarsakerInneholderEnEllerFlereRegler_test() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_0_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.aarsakerInneholderEnEllerFlereRegler(listOf("REGEL_C")))
        Assertions.assertTrue(jsonNode.aarsakerInneholderEnEllerFlereRegler(listOf("REGEL_0_1")))
        Assertions.assertTrue(jsonNode.aarsakerInneholderEnEllerFlereRegler(listOf("REGEL_0_1", "REGEL_C")))
        Assertions.assertFalse(jsonNode.aarsakerInneholderEnEllerFlereRegler(listOf("REGEL_A", "REGEL_C")))
        Assertions.assertTrue(respons.sporsmal.isEmpty())
    }

    @Test
    fun aarsakerInneholderKunEnRegle_test() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_0_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_C")))
        Assertions.assertTrue(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_0_1")))
        Assertions.assertTrue(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_0_1", "REGEL_C")))
        Assertions.assertFalse(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_A", "REGEL_C")))
        Assertions.assertTrue(respons.sporsmal.isEmpty())
    }

    @Test
    fun aarsakerInneholderKunEnReglerMedFlereaarsaker_test() {
        val fileContent =
            this::class.java.classLoader.getResource("sampleVurdering_uavklart.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_25")))
        Assertions.assertTrue(respons.sporsmal.isEmpty())

    }

    @Test
    fun regel10_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_10.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_10")
    }

    @Test
    fun aarsakerInneholderKunRegel_3_test() {
        val fileContent =
            this::class.java.classLoader.getResource("sampleVurdering_uavklart_REGEL_3.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_3")))
        Assertions.assertFalse(respons.sporsmal.isEmpty())
    }

    @Test
    fun medlbrudd_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("Medl_brudd_sample.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.aarsakerInneholderMEDLRegler(MEDL_REGLER))
        Assertions.assertFalse(respons.sporsmal.isEmpty())
    }

    @Test
    fun regel15_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_15.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_15")
    }

    @Test
    fun kombinasjonerAvEnkeltReglerIListaSkalFøreTilBrukerSpørsmål() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_3_OG_REGEL_34.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(
            respons.sporsmal.isEmpty(),
            "Det skal opprettes brukersporsmaal paa kobinasjoner av godkjente regler"
        )
    }

    @Test
    fun kombinasjonerAvEnkeltReglerOgMultiReglerIListaSkalFøreTilBrukerSpørsmål() {
        val fileContent =
            this::class.java.classLoader.getResource("REGEL_3_OG_REGEL_11_2_2_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(
            respons.sporsmal.isEmpty(),
            "Det skal opprettes brukersporsmaal paa kobinasjoner av godkjente regler"
        )
    }

    @Test
    fun Ulovlig_kombinasjonerAvEnkeltReglerOgMultiReglerIListaSkalIkkeFøreTilBrukerSpørsmål() {
        val fileContent =
            this::class.java.classLoader.getResource("REGEL_3_8_OG_11_2_2_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertTrue(
            respons.sporsmal.isEmpty(),
            "Det skal ikke opprettes brukersporsmaal paa ulovlige kombinasjoner av ikke godkjente regler"
        )
    }

    @Test
    fun regelC_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_C.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_C")
    }

    @Test
    fun regel11_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_11_2_3.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_11_2_3")
    }

    @Test
    fun regel12_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_12.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_12")
    }

    @Test
    fun regel20_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_20.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_20")
    }

    @Test
    fun regel34_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_34.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_34")
    }

    @Test
    fun regel21_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_21.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_21")
    }

    @Test
    fun regel25_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_25.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_25")
    }

    @Test
    fun regel5_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_5.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_5")
    }

    @Test
    fun regel5_i_kombinasjon_med_regel3_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_5_og_10.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_5 med REGEL_10")
    }


}