package no.nav.medlemskap.sykepenger.lytter.jackson

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test



class JacksonParserTest {

    @Test
    fun `parse Json String`() {
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        assertNotNull(sykepengeSoknad)
    }

}