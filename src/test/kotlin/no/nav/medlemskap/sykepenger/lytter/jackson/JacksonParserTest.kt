package no.nav.medlemskap.sykepenger.lytter.jackson

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.Periode
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.Svar
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Type
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.LocalDate


class JacksonParserTest {

    @Test
    fun `parser alle kjente soknadstyper uten fallback`() {
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT.json").readText(Charsets.UTF_8)
        val json = JacksonParser.mapper.readValue(fileContent, ObjectNode::class.java)

        Type.entries.forEach { type ->
            json.put("type", type.name)

            val sykepengeSoknad = requireNotNull(JacksonParser().parse(json.toString()))

            assertEquals(type, sykepengeSoknad.type, "$type skal parses uten fallback")
            assertEquals(json["id"].asText(), sykepengeSoknad.id, "$type skal beholde soknads-ID")
        }
    }

    @Test
    fun `ukjent soknadstype gir ingen soknad`() {
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT.json").readText(Charsets.UTF_8)
        val json = JacksonParser.mapper.readValue(fileContent, ObjectNode::class.java)
        json.put("type", "UKJENT_SOKNADSTYPE")

        assertNull(JacksonParser().parse(json.toString()))
    }

    @Test
    fun `ugyldig JSON gir ingen soknad`() {
        assertNull(JacksonParser().parse("{"))
    }

    @Test
    fun `manglende pakrevde felter gir ingen soknad`() {
        assertNull(JacksonParser().parse("{}"))
    }

    @Test
    fun `parsefeil logges med forklaring uten meldingsinnhold`() {
        val logger = LoggerFactory.getLogger(JacksonParser::class.java) as Logger
        val appender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(appender)
        try {
            assertNull(JacksonParser().parse("""{"id":"test","type":"verdi-som-ikke-skal-logges"}"""))
            assertNull(JacksonParser().parse("{"))
            assertNull(JacksonParser().parse("{}"))

            val meldinger = appender.list.map { it.formattedMessage }
            assertTrue(meldinger.any { it.contains("ukjent enumverdi for Type") })
            assertTrue(meldinger.any { it.contains("ugyldig JSON-syntaks") })
            assertTrue(meldinger.any { it.contains("manglende påkrevd felt eller feil JSON-struktur") })
            assertTrue(meldinger.none { it.contains("verdi-som-ikke-skal-logges") })
            assertTrue(appender.list.all { it.throwableProxy == null })
        } finally {
            logger.detachAppender(appender)
            appender.stop()
        }
    }

    @Test
    fun `soknad uten pakrevd sporsmal gir ingen soknad`() {
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        assertNull(sykepengeSoknad)
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
        val sporsmaal: Brukerspørsmål =  JacksonParser().toDomainObject(sporsmallString)
        val b = Brukerspørsmål(
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
