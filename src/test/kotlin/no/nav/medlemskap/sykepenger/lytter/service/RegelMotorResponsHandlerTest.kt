package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RegelMotorResponsHandlerTest {
    @Test
    fun eosborger(){
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_EOS_BORGER.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.erEosBorger())
        Assertions.assertFalse(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertNull(jsonNode.oppholdsTillatelsePeriode())


    }
    @Test
    fun tredjelandsBorgerGiftMedEOSPerson(){
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_3LAND_GIFTEOS_BORGER.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertTrue(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun tredjelandsBorgerGiftMedEOSPersonSomHarOppholdsTilatelse(){
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_3LAND_GIFTEOS_BORGER_MED_OPPHOLD.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertTrue(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertTrue(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun kanHenteUtPeriodeFraOppholdsTilatelse(){
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_3LAND_GIFTEOS_BORGER_MED_OPPHOLD.json").readText(Charsets.UTF_8)
        val jsonNode = objectMapper.readTree(fileContent)
        val periode = jsonNode.oppholdsTillatelsePeriode()
        Assertions.assertNotNull(periode)

    }
    @Test
    fun tredjelandsBorgerIkkeGiftMedEOSPerson(){
        val fileContent = this::class.java.classLoader.getResource("UAVKLART_3LAND_IKKE_GIFT_EOS_BORGER.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun regel_19_1(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_1.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }


    fun regel_19_3_1(){
        val fileContent = this::class.java.classLoader.getResource("respons_regelmotor_kunn_19_3_1_brudd.json").readText(Charsets.UTF_8)
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
        Assertions.assertTrue(anbefalt.sporsmal.containsAll(setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE)),"oppholdstilatelse, arbeid utenfor norge og opphold utenfor norge skal finnes")
    }
    @Test
    fun regel_19_3_1_medPermanentOpphold(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_3_1_med_Permanent.json").readText(Charsets.UTF_8)
        val anbefalt = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        val periode = RegelMotorResponsHandler().hentOppholdsTilatelsePeriode(fileContent)
        Assertions.assertNotNull(periode)
        Assertions.assertNotNull(periode!!.fom)
        Assertions.assertNull(periode!!.tom)
        // Assertions.assertTrue(anbefalt.sporsmal.containsAll(setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE)),"oppholdstilatelse, arbeid utenfor norge og opphold utenfor norge skal finnes")
    }
    @Test
    fun regel_19_8(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_8.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erEosBorger())
        Assertions.assertTrue(jsonNode.erTredjelandsborger())
        Assertions.assertFalse(jsonNode.erTredjelandsborgerMedEØSFamilie())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun regel_19_7(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_7.json").readText(Charsets.UTF_8)
        RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.erBritiskBorger())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun REGEL_0_1(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_0_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.erBritiskBorger())
        Assertions.assertFalse(jsonNode.harOppholdsTilatelse())
    }
    @Test
    fun aarsakerInneholderEnEllerFlereRegler_test(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_0_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.aarsakerInneholderEnEllerFlereRegler(listOf("REGEL_C")))
        Assertions.assertTrue(jsonNode.aarsakerInneholderEnEllerFlereRegler(listOf("REGEL_0_1")))
        Assertions.assertTrue(jsonNode.aarsakerInneholderEnEllerFlereRegler(listOf("REGEL_0_1","REGEL_C")))
        Assertions.assertFalse(jsonNode.aarsakerInneholderEnEllerFlereRegler(listOf("REGEL_A","REGEL_C")))
        Assertions.assertTrue(respons.sporsmal.isEmpty())
    }
    @Test
    fun aarsakerInneholderKunEnRegle_test(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_0_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_C")))
        Assertions.assertTrue(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_0_1")))
        Assertions.assertTrue(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_0_1","REGEL_C")))
        Assertions.assertFalse(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_A","REGEL_C")))
        Assertions.assertTrue(respons.sporsmal.isEmpty())
    }
    @Test
    fun aarsakerInneholderKunEnReglerMedFlereaarsaker_test(){
        val fileContent = this::class.java.classLoader.getResource("sampleVurdering_uavklart.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertFalse(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_25")))
        Assertions.assertTrue(respons.sporsmal.isEmpty())
    }
    @Test
    fun aarsakerInneholderKunRegel_3_test(){
        val fileContent = this::class.java.classLoader.getResource("sampleVurdering_uavklart_REGEL_3.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_3")))
        Assertions.assertFalse(respons.sporsmal.isEmpty())
    }

    @Test
    fun medlbrudd_skal_fore_til_bruersporsmaal(){
        val fileContent = this::class.java.classLoader.getResource("Medl_brudd_sample.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.aarsakerInneholderMEDLRegler(MEDL_REGLER))
        Assertions.assertFalse(respons.sporsmal.isEmpty())
    }


}