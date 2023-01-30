package no.nav.medlemskap.sykepenger.lytter.jackson

import no.nav.medlemskap.sykepenger.lytter.domain.lagMedlemskapsResultat
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
        val fileContent = this::class.java.classLoader.getResource("sampleVurdering.json").readText(Charsets.UTF_8)
        val vurdering = JacksonParser().ToJson(fileContent)
        val resultat = vurdering.lagMedlemskapsResultat()

        assertEquals(resultat.fnr, "19026500128")
        assertEquals(resultat.svar, "JA")
        assertNull(resultat.årsak)
        assertNull(resultat.årsaker.firstOrNull())

    }


}