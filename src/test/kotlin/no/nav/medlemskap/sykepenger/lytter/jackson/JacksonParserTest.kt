package no.nav.medlemskap.sykepenger.lytter.jackson

import no.nav.medlemskap.sykepenger.lytter.domain.lagMedlemskapsResultat
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Periode
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate


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
        println("20048139036".sha256())
    }

    @Test
    fun`print2`() {
        val respons = FlexRespons(
            svar = Svar.UAVKLART,
            sporsmal = setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.OPPHOLD_UTENFOR_NORGE,Spørsmål.ARBEID_UTENFOR_NORGE),
            kjentOppholdstillatelse = Periode(LocalDate.now().minusYears(1), LocalDate.now().plusMonths(10))
        )
        println(JacksonParser().ToJson(respons).toPrettyString())
    }

    @Test
    fun  `testAvInnLesingAvOppholdsPeriode`(){
        val sporsmallString = "{\"fnr\": \"872fc5dcfa0fdc0bdf8839e005757aea9a0536cbd50d845a80e011ad0eed3995\", \"status\": \"SENDT\", \"ytelse\": \"SYKEPENGER\", \"soknadid\": \"855e67bd-613c-3b9b-aa69-ba3a97c787a2\", \"eventDate\": \"2024-07-04\", \"sporsmaal\": {\"arbeidUtland\": null}, \"oppholdUtenforEOS\": null, \"oppholdstilatelse\": {\"id\": \"5b0887b7-35c8-3df1-814e-2e93c72c64a2\", \"svar\": true, \"perioder\": [{\"fom\": \"2024-07-05\", \"tom\": \"2025-07-05\"}], \"vedtaksdato\": \"2024-06-14\", \"sporsmalstekst\": \"Har Utlendingsdirektoratet gitt deg en oppholdstillatelse før 5. juli 2024?\", \"vedtaksTypePermanent\": false}, \"oppholdUtenforNorge\": {\"id\": \"92dc485e-1432-3339-9a82-2f207ff41119\", \"svar\": false, \"sporsmalstekst\": \"Har du oppholdt deg i utlandet i løpet av de siste 12 månedene før du ble syk?\", \"oppholdUtenforNorge\": []}, \"utfort_arbeid_utenfor_norge\": {\"id\": \"31c01d4a-7c35-38d9-ba0d-b87900d2bdd7\", \"svar\": false, \"sporsmalstekst\": \"Har du arbeidet utenfor Norge i løpet av de siste 12 månedene før du ble syk?\", \"arbeidUtenforNorge\": []}}"
        val sporsmaal: Brukersporsmaal =  JacksonParser().toDomainObject(sporsmallString)
        val b = Brukersporsmaal(
            fnr=sporsmaal.fnr,
            soknadid = "",
            eventDate= LocalDate.now(),
            ytelse= "",
            status= "SENT",
            sporsmaal= sporsmaal.sporsmaal,
            oppholdstilatelse = sporsmaal.oppholdstilatelse,
            utfort_arbeid_utenfor_norge = sporsmaal.utfort_arbeid_utenfor_norge,
            oppholdUtenforNorge = sporsmaal.oppholdUtenforNorge,
            oppholdUtenforEOS = sporsmaal.oppholdUtenforEOS)

        print(b)

        }

    }
