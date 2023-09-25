package no.nav.medlemskap.sykepenger.lytter.jackson

import no.nav.medlemskap.sykepenger.lytter.domain.lagMedlemskapsResultat
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test



class JacksonParserTest {

    @Test
    fun `parse Json String`() {
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        assertNotNull(sykepengeSoknad)
    }
    @Test
    fun `parse Json String2`() {
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        assertNotNull(sykepengeSoknad)
    }
    @Test
    fun `parse Json String med UTLAND data`() {
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        assertNotNull(sykepengeSoknad)
    }

    @Test
    fun `MedlemskapResultat mappes riktig`() {
        val fileContent = this::class.java.classLoader.getResource("sampleVurdering_uavklart.json").readText(Charsets.UTF_8)
        val vurdering = JacksonParser().ToJson(fileContent)
        val resultat = vurdering.lagMedlemskapsResultat()

        assertEquals("15076500565", resultat.fnr )
        assertEquals("UAVKLART", resultat.svar)
        assertEquals("REGEL_25", resultat.årsak)
        assertEquals("[\"REGEL_25\", \"REGEL_1_4\"]", resultat.årsaker.toString())
    }
    @Test
    fun `print`() {
        println("06057032667".sha256())
    }


}