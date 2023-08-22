package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.saga.persistence.Medlemskap_oppholdstilatelse_brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BrukerspormaalMapperTest {

    @Test
    fun `test mapping av flex_oppholdstilatelse bruker sporsmaal med ikke permanent oppholdstilatelse`(){
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageFlereBrukerSporsmaal_2.json").readText(Charsets.UTF_8)
        val jsonNode = JacksonParser().ToJson(fileContent)
        val mapper = BrukersporsmaalMapper(jsonNode)
        val v = mapper.getOppholdstilatelse_brukerspørsmål()
        Assertions.assertNotNull(v)
        val brukerspørsmaal = mapper.oppholdstilatelse_brukersporsmaal
        Assertions.assertNotNull(brukerspørsmaal)
        brukerspørsmaal?.let { Assertions.assertTrue(it.svar,"Bruker skal ha oppholdstilatelse") }
        brukerspørsmaal?.let { Assertions.assertFalse(it.vedtaksTypePermanent,"Bruker skal ikke ha permanent oppholdstilatelse") }
        brukerspørsmaal?.let { Assertions.assertTrue(it.perioder.size==1,"det skal finnes ett innslag i periode liste") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.vedtaksdato,"det skal finnes vedtaksdato") }
    }
    @Test
    fun `test mapping av flex_arbeidIUtenforNorge bruker sporsmaal arbeidUlandTrue`(){
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageFlereBrukerSporsmaal.json").readText(Charsets.UTF_8)
        val jsonNode = JacksonParser().ToJson(fileContent)
        val mapper = BrukersporsmaalMapper(jsonNode)
        val v = mapper.getOppholdstilatelse_brukerspørsmål()
        Assertions.assertNotNull(v)
        val brukerspørsmaal = mapper.arbeidUtlandBrukerSporsmaal
        Assertions.assertNotNull(brukerspørsmaal,"Det finnes ikke brukerspørmål mappet")
        brukerspørsmaal?.let { Assertions.assertTrue(it.svar,"Bruker skal ha ArbeidUtland") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.id,"Bruker skal satt ID på spørsmål") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.sporsmalstekst,"spørsmålstekst skal være satt") }
        brukerspørsmaal?.let { Assertions.assertTrue(it.arbeidUtenforNorge.size==2,"det skal finnes to utlandsperiode") }
    }
}