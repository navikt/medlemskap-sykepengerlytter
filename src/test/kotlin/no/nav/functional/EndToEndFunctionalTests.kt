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
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import no.nav.medlemskap.sykepenger.lytter.service.*
import no.nav.persistence.AbstractContainerDatabaseTest
import no.nav.persistence.MyPostgreSQLContainer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import java.time.LocalDateTime
import java.util.*
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
    @Test
     fun `brukerspormaal ende til ende simulering der begge bruker spørsmål har svar JA`() = runBlocking {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val soknadID = UUID.randomUUID().toString()
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val brukerspormsalRepo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        dsb.getDataSource().connection.createStatement().execute("delete  from syk_vurdering")
        val medlemskapVurdertRepo = PostgresMedlemskapVurdertRepository(dsb.getDataSource())
        val containerPersistenceService = PersistenceService(medlemskapVurdertRepo,brukerspormsalRepo)
        val bomloService = BomloService(Configuration(), containerPersistenceService)
        bomloService.lovmeClient = LovMeMock()

        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)


        /*
        * Simuler at det kommer inn et kall til bruker spørsmål api
        * */
        val medlemskapOppslagRequest = MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "2023-08-15", periode = Periode("2023-08-16","2023-08-22"),
            Brukerinput(arbeidUtenforNorge = true)
        )
        val lovmeresponse = bomloService.kallLovme(medlemskapOppslagRequest,"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val alleredeStilteSporsmaal = bomloService.hentAlleredeStilteBrukerSpørsmål(medlemskapOppslagRequest)
        val flexRespons: FlexRespons =  createFlexRespons(foreslaattRespons,alleredeStilteSporsmaal)
        println(flexRespons)
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
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr("15076500565").isNotEmpty())
        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */
        val lovmeresponse2 = bomloService.kallLovme(MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "2023-08-15", periode = Periode("2023-08-16","2023-08-22"),
            Brukerinput(arbeidUtenforNorge = true)
        ),"2345")
        val foreslaattRespons2 = RegelMotorResponsHandler().utledResultat(lovmeresponse2)
        val alleredeStilteSporsmaal2 = bomloService.hentAlleredeStilteBrukerSpørsmål(medlemskapOppslagRequest)
        val flexRespons2: FlexRespons =  createFlexRespons(foreslaattRespons2,alleredeStilteSporsmaal2)
        Assertions.assertEquals(flexRespons,flexRespons2,"respons i begge tilfellene skal være like da svar på begge brukerspørsmålene er JA")


    }

    @Test
    fun delete(){
        println("25110085802".sha256())
    }

    @Test
    fun `20 dager mellom sykmeldingsperiode 1 og 2 skal levetiden være aktiv og vi skal ikke stille spørsmål på nytt`() = runBlocking {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val brukerspormsalRepo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        dsb.getDataSource().connection.createStatement().execute("delete  from syk_vurdering")
        val medlemskapVurdertRepo = PostgresMedlemskapVurdertRepository(dsb.getDataSource())
        val containerPersistenceService = PersistenceService(medlemskapVurdertRepo,brukerspormsalRepo)
        val bomloService = BomloService(Configuration(), containerPersistenceService)
        bomloService.lovmeClient = LovMeMock()

        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)


        /*
        * Simuler at det kommer inn et kall til bruker spørsmål api
        * */
        //Periode: 2020-01-01 til 2020-01-20
        //Første dag for ytelse: 2020-01-01
        val medlemskapOppslagRequest = MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "2020-01-01", periode = Periode("2020-01-01","2020-01-20"),
            Brukerinput(arbeidUtenforNorge = true)
        )
        val lovmeresponse = bomloService.kallLovme(MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "", periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        ),"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val alleredeStilteSporsmaal = bomloService.hentAlleredeStilteBrukerSpørsmål(medlemskapOppslagRequest)
        val flexRespons: FlexRespons =  createFlexRespons(foreslaattRespons,alleredeStilteSporsmaal)
        println(flexRespons)

        //Sender inn søknad+brukersvar med NEI på begge spørsmål
        //Blir "lagret" 2020-01-02
        //SendtArbeidsgiver = 2020-01-02
        val value = this::class.java.classLoader.getResource("EndeTilEndeTestFraFlexOmSoknadForSykepenger.json").readText(Charsets.UTF_8)
        /*
        * modifiser dato i json fil (sendt arbeidsgiver) til dagens dato så ikke test bryter i fremtiden
        * */
        //val modified = value.replace("2023-08-23T13:23:22.229663373", eventdateLagretVurdering.toString())
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = value,
            key="",
            topic="",
            timestamp = LocalDateTime.of(2020,1,1,10,10),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr("15076500565").isNotEmpty())

        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */
        //Periode: 2020-01-21 til 2020-01-31
        //Første dag for ytelse: 2020-01-21
        val medlemskapOppslagRequest2 = MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "2020-01-21", periode = Periode("2020-01-21","2020-01-31"),
            Brukerinput(arbeidUtenforNorge = true)
        )
        val lovmeresponse2 = bomloService.kallLovme(MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "", periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        ),"2345")

        val foreslaattRespons2 = RegelMotorResponsHandler().utledResultat(lovmeresponse2)
        val alleredeStilteSporsmaal2 = bomloService.hentAlleredeStilteBrukerSpørsmål(medlemskapOppslagRequest2)
        val flexRespons2: FlexRespons =  createFlexRespons(foreslaattRespons2,alleredeStilteSporsmaal2)
        Assertions.assertNotEquals(flexRespons,flexRespons2,"respons i begge tilfellene skal ikke være like da svar på begge brukerspørsmålene er NEI")
        Assertions.assertTrue(flexRespons2.sporsmal.isEmpty())
    }



    @Test
    fun `1 år mellom sykmeldingsperiode 1 og 2 så skal levetiden ikke være aktiv og vi skal stille spørsmål på nytt`() = runBlocking {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val brukerspormsalRepo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        dsb.getDataSource().connection.createStatement().execute("delete  from syk_vurdering")
        val medlemskapVurdertRepo = PostgresMedlemskapVurdertRepository(dsb.getDataSource())
        val containerPersistenceService = PersistenceService(medlemskapVurdertRepo,brukerspormsalRepo)
        val bomloService = BomloService(Configuration(), containerPersistenceService)
        bomloService.lovmeClient = LovMeMock()

        val fmh = FlexMessageHandler(Configuration(),containerPersistenceService)


        /*
        * Simuler at det kommer inn et kall til bruker spørsmål api
        * */
        //Periode: 2020-01-01 til 2020-01-20
        //Første dag for ytelse: 2020-01-01
        val medlemskapOppslagRequest = MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "2020-01-01", periode = Periode("2020-01-01","2020-01-20"),
            Brukerinput(arbeidUtenforNorge = true)
        )
        val lovmeresponse = bomloService.kallLovme(MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "", periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        ),"2345")
        val foreslaattRespons = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val alleredeStilteSporsmaal = bomloService.hentAlleredeStilteBrukerSpørsmål(medlemskapOppslagRequest)
        val flexRespons: FlexRespons =  createFlexRespons(foreslaattRespons,alleredeStilteSporsmaal)
        println(flexRespons)

        //Sender inn søknad+brukersvar med NEI på begge spørsmål
        //Blir "lagret" 2020-01-02
        //SendtArbeidsgiver = 2020-01-02
        val value = this::class.java.classLoader.getResource("EndeTilEndeTestFraFlexOmSoknadForSykepenger.json").readText(Charsets.UTF_8)
        /*
        * modifiser dato i json fil (sendt arbeidsgiver) til dagens dato så ikke test bryter i fremtiden
        * */
        //val modified = value.replace("2023-08-23T13:23:22.229663373", eventdateLagretVurdering.toString())
        /*
        * Simuler at det kommer inn en melding på kafka med disse bruker spørsmålene
        * */
        val message = FlexMessageRecord(
            partition = 0,
            offset = 1,
            value = value,
            key="",
            topic="",
            timestamp = LocalDateTime.of(2020,1,1,10,10),
            timestampType = ""
        )
        fmh.handle(message)
        Assertions.assertTrue(containerPersistenceService.hentbrukersporsmaalForFnr("15076500565").isNotEmpty())

        /*
       * Simuler at det kommer inn et nytt kall til bruker spørsmål api på samme bruker
       * */
        //Periode: 2021-01-01 til 2021-01-20
        //Første dag for ytelse: 2021-01-01
        val medlemskapOppslagRequest2 = MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "2021-01-01", periode = Periode("2021-01-01","2021-01-20"),
            Brukerinput(arbeidUtenforNorge = true)
        )
        val lovmeresponse2 = bomloService.kallLovme(MedlOppslagRequest(fnr = "15076500565", førsteDagForYtelse = "", periode = Periode("",""),
            Brukerinput(arbeidUtenforNorge = true)
        ),"2345")

        val foreslaattRespons2 = RegelMotorResponsHandler().utledResultat(lovmeresponse2)
        val alleredeStilteSporsmaal2 = bomloService.hentAlleredeStilteBrukerSpørsmål(medlemskapOppslagRequest2)
        val flexRespons2: FlexRespons =  createFlexRespons(foreslaattRespons2,alleredeStilteSporsmaal2)
        Assertions.assertEquals(flexRespons,flexRespons2,"respons i begge tilfellene skal være like")
        Assertions.assertTrue(flexRespons2.sporsmal.isNotEmpty())
    }

}