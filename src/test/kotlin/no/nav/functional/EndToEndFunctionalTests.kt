package no.nav.functional

import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.FlexMessageRecord
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import no.nav.medlemskap.sykepenger.lytter.service.*
import no.nav.persistence.AbstractContainerDatabaseTest
import no.nav.persistence.MyPostgreSQLContainer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import java.time.LocalDateTime
import java.util.logging.Level
import java.util.logging.LogManager

@org.testcontainers.junit.jupiter.Testcontainers
class EndToEndFunctionalTests : AbstractContainerDatabaseTest() {
    init {
        // Postgres JDBC driver uses JUL; disable it to avoid annoying, irrelevant, stderr logs during connection testing
        LogManager.getLogManager().getLogger("").level = Level.OFF
    }
    companion object {
        // will be shared between test methods
        @Container
        private val postgresqlContainer     = MyPostgreSQLContainer("postgres:14")
            .withDatabaseName("medlemskap")
            .withUsername("postgres")
            .withPassword("test")
    }


    //Testcaser som fører til nye spørsmål
    @Test
    fun `skal anbefale nytt spørsmålssett - har ingen brukerspørsmål fra før`() = runBlocking {
        val containerPersistenceService = settOppKonfig()
        val bomloService = BomloService(Configuration(), containerPersistenceService)

        bomloService.lovmeClient = LovMeApiMock(
            mapOf(
                "vurderMedlemskap" to "sampleVurdering.json",
                "vurderMedlemskapBomlo" to "sampleVurdering.json",
                "brukerspørsmål" to "vurdering_eos_borger_uavklart_REGEL_3.json"
            )
        )

        //Steg 1: Bruker blir syk for første gang
        val førsteDagForYtelse_mockData = "2023-08-16"
        val testperson = "15076500565"
        val lovmeRequest = MedlOppslagRequest(fnr = testperson, førsteDagForYtelse = førsteDagForYtelse_mockData, periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        )

        val lovmeresponse = bomloService.kallLovme(lovmeRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons: FlexRespons = opprettResponsTilFlex(foreslaattRespons, forrigeBrukerspørsmål)

        val forventedeForeslåtteSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val forventedeForrigeBrukerspørsmål = emptyList<Spørsmål>()
        val forventedeSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)

        Assertions.assertEquals(forventedeForeslåtteSpørsmål, foreslaattRespons.sporsmal, "Foreslåtte spørsmål")
        Assertions.assertEquals(forventedeForrigeBrukerspørsmål, forrigeBrukerspørsmål, "Forrige brukerspørsmål")
        Assertions.assertEquals(forventedeSpørsmål, flexRespons.sporsmal, "Anbefalte")
    }

    @Test
     fun `skal anbefale nytt spørsmålssett - for lang tid mellom nåværende og forrige søknad`() = runBlocking {
        val containerPersistenceService = settOppKonfig()
        val bomloService = BomloService(Configuration(), containerPersistenceService)

        bomloService.lovmeClient = LovMeApiMock(
            mapOf(
                "vurderMedlemskap" to "sampleVurdering.json",
                "vurderMedlemskapBomlo" to "sampleVurdering.json",
                "brukerspørsmål" to "vurdering_eos_borger_uavklart_REGEL_3.json"
            )
        )

        val testperson = "15076500565"

        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)

        //Steg 1: Forrige søknad om sykmelding med brukersvar fra mock data i json fil
        //førsteDagForYtelse: "2023-08-16"
        //sendtArbeidsgiver/eventDate="2023-08-23"
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = this::class.java.classLoader.getResource("EndeTilEndeTestEOSBrukerSoknadFraFlex.json").readText(Charsets.UTF_8),
            key="",
            topic="",
            timestamp = LocalDateTime.now(),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr(testperson).isNotEmpty())
        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */

        //Steg 2: Bruker blir syk igjen og det skal sjekkes om forrige brukerspørsmål skal gjenbrukes
        val førsteDagForYtelse_mockData = "2024-08-16"
        val lovmeRequest = MedlOppslagRequest(fnr = testperson, førsteDagForYtelse = førsteDagForYtelse_mockData, periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = false)
        )

        val lovmeresponse = bomloService.kallLovme(lovmeRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons: FlexRespons =  opprettResponsTilFlex(foreslaattRespons, forrigeBrukerspørsmål)

        val forventedeForeslåtteSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val forventedeforrigeBrukerspørsmål = emptyList<Spørsmål>()
        val forventedeSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)

        Assertions.assertEquals(forventedeForeslåtteSpørsmål, foreslaattRespons.sporsmal, "Foreslåtte spørsmål")
        Assertions.assertEquals(forventedeforrigeBrukerspørsmål, forrigeBrukerspørsmål, "Forrige brukerspørsmål")
        Assertions.assertEquals(forventedeSpørsmål, flexRespons.sporsmal, "Anbefalte")
    }

    @Test
    fun `skal anbefale nytt spørsmålssett - kort mellom og nytt spørsmålssett har flere spørsmål`() = runBlocking {
        val containerPersistenceService = settOppKonfig()
        val bomloService = BomloService(Configuration(), containerPersistenceService)

        bomloService.lovmeClient = LovMeApiMock(
            mapOf(
                "vurderMedlemskap" to "sampleVurdering.json",
                "vurderMedlemskapBomlo" to "sampleVurdering.json",
                "brukerspørsmål" to "vurdering_andre_borger_uavklart.json"
            )
        )

        val testperson = "15076500565"
        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)

        //Steg 1: Forrige søknad om sykmelding med brukersvar fra mock data i json fil
        //førsteDagForYtelse "fom": "2023-08-16", eventDate="2023-08-23"
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = this::class.java.classLoader.getResource("EndeTilEndeTestEOSBrukerSoknadFraFlex.json").readText(Charsets.UTF_8),
            key="",
            topic="",
            timestamp = LocalDateTime.now(),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr(testperson).isNotEmpty())
        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */

        //Steg 2: Bruker blir syk igjen
        val førsteDagForYtelse_mockData = "2023-08-30"
        val lovmeRequest = MedlOppslagRequest(fnr = testperson, førsteDagForYtelse = førsteDagForYtelse_mockData, periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = false)
        )

        val lovmeresponse = bomloService.kallLovme(lovmeRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons: FlexRespons =  opprettResponsTilFlex(foreslaattRespons, forrigeBrukerspørsmål)

        val forventedeForeslåtteSpørsmål = setOf(Spørsmål.OPPHOLDSTILATELSE, Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_NORGE)
        val forventedeForrigeBrukerspørsmål = listOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val forventedeSpørsmål = setOf(Spørsmål.OPPHOLDSTILATELSE, Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_NORGE)

        Assertions.assertEquals(forventedeForeslåtteSpørsmål, foreslaattRespons.sporsmal, "Foreslåtte spørsmål")
        Assertions.assertEquals(forventedeForrigeBrukerspørsmål, forrigeBrukerspørsmål, "Forrige brukerspørsmål")
        Assertions.assertEquals(forventedeSpørsmål, flexRespons.sporsmal, "Anbefalte")
    }

    @Test
    fun `skal anbefale nytt spørsmålssett - kort mellom og overlapper delvis med nytt spørsmålssett`() = runBlocking {
        val containerPersistenceService = settOppKonfig()
        val bomloService = BomloService(Configuration(), containerPersistenceService)

        bomloService.lovmeClient = LovMeApiMock(
            mapOf(
                "vurderMedlemskap" to "sampleVurdering.json",
                "vurderMedlemskapBomlo" to "sampleVurdering.json",
                "brukerspørsmål" to "vurdering_andre_borger_uavklart_med_opphold.json"
            )
        )

        val testperson = "15076500565"
        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)

        //Steg 1: Forrige søknad om sykmelding med brukersvar fra mock data i json fil
        //førsteDagForYtelse "fom": "2023-08-16", eventDate="2023-08-23"
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = this::class.java.classLoader.getResource("EndeTilEndeTestEOSBrukerSoknadFraFlex.json").readText(Charsets.UTF_8),
            key="",
            topic="",
            timestamp = LocalDateTime.now(),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr(testperson).isNotEmpty())
        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */

        //Steg 2: Bruker blir syk igjen
        val førsteDagForYtelse_mockData = "2023-08-30"
        val lovmeRequest = MedlOppslagRequest(fnr = testperson, førsteDagForYtelse = førsteDagForYtelse_mockData, periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        )

        val lovmeresponse = bomloService.kallLovme(lovmeRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons: FlexRespons =  opprettResponsTilFlex(foreslaattRespons, forrigeBrukerspørsmål)

        val forventedeForeslåtteSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_NORGE)
        val forventedeForrigeBrukerspørsmål = listOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val forventedeSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_NORGE)

        Assertions.assertEquals(forventedeForeslåtteSpørsmål, foreslaattRespons.sporsmal, "Foreslåtte spørsmål")
        Assertions.assertEquals(forventedeForrigeBrukerspørsmål, forrigeBrukerspørsmål, "Forrige brukerspørsmål")
        Assertions.assertEquals(forventedeSpørsmål, flexRespons.sporsmal, "Anbefalte")
    }

    @Test
    fun `skal anbefale nytt spørsmålssett - kort mellom inneholder JA svar og overlapper delvis med nytt spørsmålssett`() = runBlocking {
        val containerPersistenceService = settOppKonfig()
        val bomloService = BomloService(Configuration(), containerPersistenceService)

        bomloService.lovmeClient = LovMeApiMock(
            mapOf(
                "vurderMedlemskap" to "sampleVurdering.json",
                "vurderMedlemskapBomlo" to "sampleVurdering.json",
                "brukerspørsmål" to "vurdering_eos_borger_uavklart_REGEL_3.json"
            )
        )

        val testperson = "15076500565"
        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)

        //Steg 1: Forrige søknad om sykmelding med brukersvar fra mock data i json fil
        //førsteDagForYtelse "fom": "2023-08-16", eventDate="2023-08-23"
        //Bruker har svart JA på arbeid utenfor norge
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = this::class.java.classLoader.getResource("EndeTilEndeTestEOSArbeidUtenforNorgeJa.json").readText(Charsets.UTF_8),
            key="",
            topic="",
            timestamp = LocalDateTime.now(),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr(testperson).isNotEmpty())
        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */

        //Steg 2: Bruker blir syk igjen
        val førsteDagForYtelse_mockData = "2023-08-30"
        val lovmeRequest = MedlOppslagRequest(fnr = testperson, førsteDagForYtelse = førsteDagForYtelse_mockData, periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = false)
        )

        val lovmeresponse = bomloService.kallLovme(lovmeRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons: FlexRespons =  opprettResponsTilFlex(foreslaattRespons, forrigeBrukerspørsmål)

        val forventedeForeslåtteSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val forventedeForrigeBrukerspørsmål = listOf(Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val forventedeSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)

        Assertions.assertEquals(forventedeForeslåtteSpørsmål, foreslaattRespons.sporsmal, "Foreslåtte spørsmål")
        Assertions.assertEquals(forventedeForrigeBrukerspørsmål, forrigeBrukerspørsmål, "Forrige brukerspørsmål")
        Assertions.assertEquals(forventedeSpørsmål, flexRespons.sporsmal, "Anbefalte")
    }

    @Test
    fun `skal anbefale nytt spørsmålssett - kort mellom med kun JA svar i forrige brukersvar`() = runBlocking {
        val containerPersistenceService = settOppKonfig()
        val bomloService = BomloService(Configuration(), containerPersistenceService)

        bomloService.lovmeClient = LovMeApiMock(
            mapOf(
                "vurderMedlemskap" to "sampleVurdering.json",
                "vurderMedlemskapBomlo" to "sampleVurdering.json",
                "brukerspørsmål" to "vurdering_eos_borger_uavklart_REGEL_3.json"
            )
        )

        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)

        //Steg 1: Forrige søknad om sykmelding med brukersvar fra mock data json fil
        //førsteDagForYtelse "fom": "2023-08-16", eventDate="2023-08-23"
        //Bruker har svart JA på arbeid utenfor norge og Ja på opphold utenfor EØS
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = this::class.java.classLoader.getResource("EndeTilEndeTestEOSArbeidUtenforNorgeJaUtenforEOSJa.json").readText(Charsets.UTF_8),
            key="",
            topic="",
            timestamp = LocalDateTime.now(),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr("15076500565").isNotEmpty())
        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */

        //Steg 2: Bruker blir syk igjen
        val førsteDagForYtelse_mockData = "2023-08-30"
        val lovmeRequest = MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = førsteDagForYtelse_mockData, periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        )

        val lovmeresponse = bomloService.kallLovme(lovmeRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons: FlexRespons =  opprettResponsTilFlex(foreslaattRespons, forrigeBrukerspørsmål)


        val forventedeForeslåtteSpørsmål= setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val forventedeForrigeBrukerspørsmål = emptyList<Spørsmål>()
        val forventedeSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)

        Assertions.assertEquals(forventedeForeslåtteSpørsmål, foreslaattRespons.sporsmal, "Foreslåtte spørsmål")
        Assertions.assertEquals(forventedeForrigeBrukerspørsmål, forrigeBrukerspørsmål, "Forrige brukerspørsmål")
        Assertions.assertEquals(forventedeSpørsmål, flexRespons.sporsmal, "Anbefalte")
    }


    //Testcaser som fører til ingen nye spørsmål
    @Test
    fun `skal ikke bruke nytt spørsmålssett - kort mellom hvor nytt spørsmålssett er et subset av forrige brukersvar`() = runBlocking {
        val containerPersistenceService = settOppKonfig()
        val bomloService = BomloService(Configuration(), containerPersistenceService)

        bomloService.lovmeClient = LovMeApiMock(
            mapOf(
                "vurderMedlemskap" to "sampleVurdering.json",
                "vurderMedlemskapBomlo" to "sampleVurdering.json",
                "brukerspørsmål" to "vurdering_andre_borger_med_eos_familie_uavklart_brudd_23.json"
            )
        )

        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)

        //Steg 1: Forrige søknad om sykmelding med brukersvar fra mock data i json fil
        //førsteDagForYtelse "fom": "2023-08-16", eventDate="2023-08-23"
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = this::class.java.classLoader.getResource("EndeTilEndeTestAndreBorgere.json").readText(Charsets.UTF_8),
            key="",
            topic="",
            timestamp = LocalDateTime.now(),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr("15076500565").isNotEmpty())
        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */

        //Steg 2: Bruker blir syk igjen
        val førsteDagForYtelse_mockData = "2023-08-30"
        val lovmeRequest = MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = førsteDagForYtelse_mockData, periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        )

        val lovmeresponse = bomloService.kallLovme(lovmeRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons: FlexRespons =  opprettResponsTilFlex(foreslaattRespons, forrigeBrukerspørsmål)

        val forventedeForeslåtteSpørsmål= setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val forventedeForrigeBrukerspørsmål = listOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE, Spørsmål.OPPHOLDSTILATELSE)
        val forventedeSpørsmål = emptySet<Spørsmål>()

        Assertions.assertEquals(forventedeForeslåtteSpørsmål, foreslaattRespons.sporsmal, "Foreslåtte spørsmål")
        Assertions.assertEquals(forventedeForrigeBrukerspørsmål, forrigeBrukerspørsmål, "Forrige brukerspørsmål")
        Assertions.assertEquals(forventedeSpørsmål, flexRespons.sporsmal, "Anbefalte")
    }


    @Test
    fun `skal ikke bruke nytt spørsmålssett - kort mellom hvor nytt spørsmålssett identisk med forrige brukersvar`() = runBlocking {
        val containerPersistenceService = settOppKonfig()
        val bomloService = BomloService(Configuration(), containerPersistenceService)

        bomloService.lovmeClient = LovMeApiMock(
            mapOf(
                "vurderMedlemskap" to "sampleVurdering.json",
                "vurderMedlemskapBomlo" to "sampleVurdering.json",
                "brukerspørsmål" to "vurdering_andre_borger_med_eos_familie_uavklart.json"
            )
        )

        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)

        //Steg 1: Forrige søknad om sykmelding med brukersvar fra mock data i json fil
        //førsteDagForYtelse "fom": "2023-08-16", eventDate="2023-08-23"
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = this::class.java.classLoader.getResource("EndeTilEndeTestAndreBorgere.json").readText(Charsets.UTF_8),
            key="",
            topic="",
            timestamp = LocalDateTime.now(),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr("15076500565").isNotEmpty())
        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */

        //Steg 2: Bruker blir syk igjen
        val førsteDagForYtelse_mockData = "2023-08-30"
        val lovmeRequest = MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = førsteDagForYtelse_mockData, periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        )

        val lovmeresponse = bomloService.kallLovme(lovmeRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons: FlexRespons =  opprettResponsTilFlex(foreslaattRespons, forrigeBrukerspørsmål)


        val forventedeForeslåtteSpørsmål = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE, Spørsmål.OPPHOLDSTILATELSE)
        val forventedeForrigeBrukerspørsmål = listOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE, Spørsmål.OPPHOLDSTILATELSE)
        val forventedeSpørsmål = emptySet<Spørsmål>()

        Assertions.assertEquals(forventedeForeslåtteSpørsmål, foreslaattRespons.sporsmal, "Foreslåtte spørsmål")
        Assertions.assertEquals(forventedeForrigeBrukerspørsmål, forrigeBrukerspørsmål, "Forrige brukerspørsmål")
        Assertions.assertEquals(forventedeSpørsmål, flexRespons.sporsmal, "Anbefalte")
    }


    @Test
    fun `skal ikke bruke nytt spørsmålssett - kort mellom med ingen nye spørsmålsett fordi bruker har gått til JA`() = runBlocking {
        val containerPersistenceService = settOppKonfig()
        val bomloService = BomloService(Configuration(), containerPersistenceService)

        bomloService.lovmeClient = LovMeApiMock(
            mapOf(
                "vurderMedlemskap" to "sampleVurdering.json",
                "vurderMedlemskapBomlo" to "sampleVurdering.json",
                "brukerspørsmål" to "vurdering_andre_borger_med_eos_familie_ja.json"
            )
        )

        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)

        //Steg 1: Forrige søknad om sykmelding med brukersvar fra mock data i json fil
        //førsteDagForYtelse "fom": "2023-08-16", eventDate="2023-08-23"
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = this::class.java.classLoader.getResource("EndeTilEndeTestAndreBorgere.json").readText(Charsets.UTF_8),
            key="",
            topic="",
            timestamp = LocalDateTime.now(),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr("15076500565").isNotEmpty())
        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */

        //Steg 2: Bruker blir syk igjen
        val førsteDagForYtelse_mockData = "2023-08-30"
        val lovmeRequest = MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = førsteDagForYtelse_mockData, periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        )

        val lovmeresponse = bomloService.kallLovme(lovmeRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons: FlexRespons =  opprettResponsTilFlex(foreslaattRespons, forrigeBrukerspørsmål)

        val forventedeForeslåtteSpørsmål = emptySet<Spørsmål>()
        val forventedeForrigeBrukerspørsmål = listOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE, Spørsmål.OPPHOLDSTILATELSE)
        val forventedeSpørsmål = emptySet<Spørsmål>()

        Assertions.assertEquals(forventedeForeslåtteSpørsmål, foreslaattRespons.sporsmal, "Foreslåtte spørsmål")
        Assertions.assertEquals(forventedeForrigeBrukerspørsmål, forrigeBrukerspørsmål, "Forrige brukerspørsmål")
        Assertions.assertEquals(forventedeSpørsmål, flexRespons.sporsmal, "Anbefalte")
    }


    @Test
    fun delete(){
        println("25110085802".sha256())
    }

    fun settOppKonfig(): PersistenceService {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val brukerspormsalRepo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        dsb.getDataSource().connection.createStatement().execute("delete  from syk_vurdering")
        val medlemskapVurdertRepo = PostgresMedlemskapVurdertRepository(dsb.getDataSource())

        return PersistenceService(medlemskapVurdertRepo,brukerspormsalRepo)

    }

}