package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar

import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BrukerspormaalMapperTest {

    @Test
    fun `test mapping av flex_oppholdstilatelse bruker sporsmaal med ikke permanent oppholdstilatelse`(){
        val mapper = BrukersporsmaalMapper(sporsmalFra("FlexSampleMessageFlereBrukerSporsmaal_komplett.json"))
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
    fun `test mapping av flex_oppholdstilatelseV2 `(){
        val mapper = BrukersporsmaalMapper(sporsmalFra("FlexSampleMessageFlereBrukerSporsmaal_komplett.json"))
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
    fun `test mapping av flex_oppholdstilatelseV2_MedNeiISvaret `(){
        val mapper = BrukersporsmaalMapper(sporsmalFra("FlexSampleMessageFlereBrukerSporsmaal_komplett_test2.json"))
        val v = mapper.getOppholdstilatelse_brukerspørsmål()
        Assertions.assertNotNull(v)
        val brukerspørsmaal = mapper.oppholdstilatelse_brukersporsmaal
        Assertions.assertNotNull(brukerspørsmaal)
        Assertions.assertFalse(brukerspørsmaal!!.svar)

    }
    @Test
    fun `test mapping av flex_oppholdstilatelse bruker sporsmaal med  permanent oppholdstilatelse`(){
        val mapper = BrukersporsmaalMapper(sporsmalFra("FlexSampleMessageFlereBrukerSporsmaal_komplett_permanent.json"))
        val v = mapper.getOppholdstilatelse_brukerspørsmål()
        Assertions.assertNotNull(v)
        val brukerspørsmaal = mapper.oppholdstilatelse_brukersporsmaal
        Assertions.assertNotNull(brukerspørsmaal)
        brukerspørsmaal?.let { Assertions.assertTrue(it.svar,"Bruker skal ha oppholdstilatelse") }
        brukerspørsmaal?.let { Assertions.assertTrue(it.vedtaksTypePermanent,"Bruker skal ikke ha permanent oppholdstilatelse") }
        brukerspørsmaal?.let { Assertions.assertTrue(it.perioder.size==1,"det skal  finnes ett innslag i periode liste") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.vedtaksdato,"det skal finnes vedtaksdato") }
    }
    @Test
    fun `test mapping av flex_arbeidIUtenforNorge bruker sporsmaal arbeidUlandTrue`(){
        val mapper = BrukersporsmaalMapper(sporsmalFra("FlexSampleMessageFlereBrukerSporsmaal.json"))

        val brukerspørsmaal = mapper.arbeidUtlandBrukerSporsmaal
        Assertions.assertNotNull(brukerspørsmaal,"Det finnes ikke brukerspørmål mappet")
        brukerspørsmaal?.let { Assertions.assertTrue(it.svar,"Bruker skal ha ArbeidUtland") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.id,"Bruker skal satt ID på spørsmål") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.sporsmalstekst,"spørsmålstekst skal være satt") }
        brukerspørsmaal?.let { Assertions.assertTrue(it.arbeidUtenforNorge.size==2,"det skal finnes to utlandsperiode") }
    }
    @Test
    fun `test mapping av flex_OppholdUtenforNorge bruker sporsmaal True`(){
        val mapper = BrukersporsmaalMapper(sporsmalFra("FlexSampleMessageFlereBrukerSporsmaal.json"))
        val brukerspørsmaal = mapper.oppholdUtenforNorge
        Assertions.assertNotNull(brukerspørsmaal,"Det finnes ikke brukerspørmål mappet")
        brukerspørsmaal?.let { Assertions.assertTrue(it.svar,"Bruker skal ha ArbeidUtland") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.id,"Bruker skal satt ID på spørsmål") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.sporsmalstekst,"spørsmålstekst skal være satt") }
        brukerspørsmaal?.let { Assertions.assertTrue(it.oppholdUtenforNorge.size==1,"det skal finnes et opphold utenfor norge") }
    }
    @Test
    fun `test mapping av flex_OppholdUtenforEOS_bruker sporsmaal True`(){
        val mapper = BrukersporsmaalMapper(sporsmalFra("FlexSampleMessageFlereBrukerSporsmaal_EOS.json"))
        val brukerspørsmaal = mapper.oppholdUtenforEOS
        Assertions.assertNotNull(brukerspørsmaal,"Det finnes ikke brukerspørmål mappet")
        brukerspørsmaal?.let { Assertions.assertTrue(it.svar,"Bruker skal ha ArbeidUtland") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.id,"Bruker skal satt ID på spørsmål") }
        brukerspørsmaal?.let { Assertions.assertNotNull(it.sporsmalstekst,"spørsmålstekst skal være satt") }
        brukerspørsmaal?.let { Assertions.assertTrue(it.oppholdUtenforEOS.size==1,"det skal finnes et opphold utenfor norge") }
    }

    private fun sporsmalFra(resourceName: String) =
        JacksonParser()
            .ToJson(this::class.java.classLoader.getResource(resourceName).readText(Charsets.UTF_8))
            .get("sporsmal")
}
