package no.nav.functional

import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar
import no.nav.medlemskap.sykepenger.lytter.service.RegelMotorResponsHandler
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BrukersporsmaalFunctionalTests {

    /*
    * SP1100 - funksjonelt krav 7
    * */
    @Test
    fun `Nei svar fra medlemskap oppslag skal føre til tom liste i anbefalte brukerspørsmål`(){
        val fileContent = this::class.java.classLoader.getResource("neiRespons.json").readText(Charsets.UTF_8)
        val anbefalt = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertEquals(Svar.NEI,anbefalt.svar,"Svar skal være NEI")
        Assertions.assertTrue(anbefalt.sporsmal.isEmpty(),"Ingen brukerspørsmpl skal anbefales når NEI svar gis fra regelmotor")


    }

    /*
    * SP1100 - funksjonelt krav 6
    * */
    @Test
    fun `Brudd på regel 19_1 - Kun regelbrudd for regel 3 skal føre til brukerspørsmål`(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_19_1.json").readText(Charsets.UTF_8)
        val anbefalt = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertEquals(Svar.UAVKLART,anbefalt.svar,"Svar skal være UAVKLART")
        Assertions.assertTrue(anbefalt.sporsmal.isEmpty(),"Ingen brukerspørsmpl skal anbefales når ikke korrekte regler bryter")

    }
    /*
    * SP1100 - funksjonelt krav 6
    * */
    @Test
    fun `Brudd på regel REGEL_3 - Kun regelbrudd for regel 3 skal føre til brukerspørsmål`(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_3.json").readText(Charsets.UTF_8)
        val anbefalt = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertEquals(Svar.UAVKLART,anbefalt.svar,"Svar skal være UAVKLART")
        Assertions.assertFalse(anbefalt.sporsmal.isEmpty(),"brukerspørsmpl skal anbefales når regel3 bryter")

    }

    /*
   * SP1100 - funksjonelt krav 4
   * */
    @Test
    fun `EOS og norske borgere skal få korrekte spørsmål`(){
        val fileContent = this::class.java.classLoader.getResource("REGEL_3.json").readText(Charsets.UTF_8)
        val anbefalt = RegelMotorResponsHandler().interpretLovmeRespons(fileContent)
        Assertions.assertEquals(Svar.UAVKLART,anbefalt.svar,"Svar skal være UAVKLART")
        Assertions.assertFalse(anbefalt.sporsmal.isEmpty(),"brukerspørsmpl skal anbefales når regel3 bryter")
        Assertions.assertTrue(anbefalt.sporsmal.contains(Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE),"brukerspørsmpl OPPHOLD_UTENFOR_EØS_OMRÅDE skal finnes")
        Assertions.assertTrue(anbefalt.sporsmal.contains(Spørsmål.ARBEID_UTENFOR_NORGE),"brukerspørsmpl OPPHOLD_UTENFOR_NORGE skal finnes")
    }
}