package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
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
    fun aarsakerInneholderKunRegel_C_test(){
        val fileContent = this::class.java.classLoader.getResource("sampleVurdering_uavklart_REGEL_C.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        val jsonNode = objectMapper.readTree(fileContent)
        Assertions.assertTrue(jsonNode.aarsakerInneholderKunEnReglel(listOf("REGEL_C")))
        Assertions.assertFalse(respons.sporsmal.isEmpty())
    }


}