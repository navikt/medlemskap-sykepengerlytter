package no.nav.persistence

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using

import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.*

import no.nav.medlemskap.sykepenger.lytter.security.sha256

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import java.time.LocalDate
import java.util.*
import java.util.logging.Level
import java.util.logging.LogManager


class MyPostgreSQLContainer(imageName: String) : PostgreSQLContainer<MyPostgreSQLContainer>(imageName)
@org.testcontainers.junit.jupiter.Testcontainers
class RepositoryTests : AbstractContainerDatabaseTest() {
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
    fun `lagre medlemskap vurdering`() {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();
        val repo = PostgresMedlemskapVurdertRepository(dsb.getDataSource())
        repo.lagreVurdering(VurderingDao(UUID.randomUUID().toString(),"1234", LocalDate.of(2020,1,1), LocalDate.of(2020,1,10),"JA"))
        repo.lagreVurdering(VurderingDao(UUID.randomUUID().toString(),"1234", LocalDate.of(2020,1,11), LocalDate.of(2020,1,20),"UAVKLART"))
        repo.lagreVurdering(VurderingDao(UUID.randomUUID().toString(),"1234", LocalDate.of(2020,1,21), LocalDate.of(2020,1,29),"PÅFØLGENDE"))


        assertNotNull("complete")
        val result = repo.finnVurdering("1234")

        assertTrue(result.size==3,"result set should contain 3 elements")

        assertEquals("1234".sha256(),result.first().fnr)
    }
    @Test
    fun `lagre påfølgende vurdering`() {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val repo = PostgresMedlemskapVurdertRepository(dsb.getDataSource())
        repo.lagreVurdering(VurderingDao("2222","2222", LocalDate.now(), LocalDate.now(),ErMedlem.PAFOLGENDE.toString()))
        val result = repo.finnVurdering("2222")

        assertTrue(result.size==1,"result set should contain 3 elements")

        assertEquals("2222".sha256(),result.first().fnr)
        assertEquals(ErMedlem.PAFOLGENDE,ErMedlem.valueOf(result.first().status))
    }
    @Test
    fun `lagre brukersporsmaal MED flexBrukerSpørsmål`() {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val soknadID = UUID.randomUUID().toString()
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val repo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        repo.lagreBrukersporsmaal(Brukersporsmaal(
            fnr="2222",
            soknadid =soknadID,
            eventDate = LocalDate.now(),
            ytelse = "SYKEPENGER",
            status="SENDT",
            sporsmaal = FlexBrukerSporsmaal(false)

        ))
        val result = repo.finnBrukersporsmaal("2222")
        val brukersporsmaal = result.first();
        assertNotNull(brukersporsmaal.sporsmaal)
        assertEquals(false,brukersporsmaal.sporsmaal!!.arbeidUtland,"arbeid utland skal være satt til false")
        assertEquals("2222".sha256(),brukersporsmaal.fnr,"fnr er ikke korrekt")

    }
    @Test
    fun `lagre brukersporsmaal MED flexBrukerSpørsmål inklusive medlemskapssporsmaal`() {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val soknadID = UUID.randomUUID().toString()
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val repo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        repo.lagreBrukersporsmaal(Brukersporsmaal(
            fnr="2222",
            soknadid =soknadID,
            eventDate = LocalDate.now(),
            ytelse = "SYKEPENGER",
            status="SENDT",
            sporsmaal = FlexBrukerSporsmaal(false),
            oppholdstilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
                id=UUID.randomUUID().toString(),
                sporsmalstekst = "Har du oppholdstillatelse fra utlendingsdirektoratet?",
                svar = true,
                vedtaksdato = LocalDate.now(),
                vedtaksTypePermanent = false,
                perioder = listOf(Periode(LocalDate.now(), LocalDate.now())),
            ),
            utfort_arbeid_utenfor_norge = Medlemskap_utfort_arbeid_utenfor_norge(
                id = UUID.randomUUID().toString(),
                sporsmalstekst = "",
                svar = true,
                arbeidUtenforNorge = listOf(ArbeidUtenforNorge(
                    id = "1",
                    arbeidsgiver = "",
                    land = "SWE",
                    perioder = listOf(Periode(LocalDate.now(), LocalDate.now()))
                ))
            )



        ))
        val result = repo.finnBrukersporsmaal("2222")
        val brukersporsmaal = result.first();
        assertNotNull(brukersporsmaal.sporsmaal)
        assertEquals(false,brukersporsmaal.sporsmaal!!.arbeidUtland,"arbeid utland skal være satt til false")
        assertEquals("2222".sha256(),brukersporsmaal.fnr,"fnr er ikke korrekt")

    }
    @Test
    fun `lagre brukersporsmaal MED flexBrukerSpørsmålGammelModell`() {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val soknadID = UUID.randomUUID().toString()
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val repo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        val json = JacksonParser().ToJson("{\"arbeidUtland\": false}")
        val fnr = "2222"
        using(sessionOf(dsb.getDataSource())) { session ->
            session.transaction {
                it.run(queryOf("INSERT INTO brukersporsmaal(fnr,soknadid, eventDate,ytelse,status,sporsmaal) VALUES(?, ?, ?, ?, ?, to_json(?::json))",fnr.sha256(),"1", LocalDate.now(),"SYKEPENGER", "SENDT",
                    json.toPrettyString()).asExecute)
            }

        }

        val result = repo.finnBrukersporsmaal("2222")
        val brukersporsmaal = result.first();
        assertNotNull(brukersporsmaal.sporsmaal)
        assertEquals(false,brukersporsmaal.sporsmaal!!.arbeidUtland,"arbeid utland skal være satt til false")
        assertEquals("2222".sha256(),brukersporsmaal.fnr,"fnr er ikke korrekt")

    }
    @Test
    fun `Hente brukersporsmaal basert paa ID`() {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val soknadID = UUID.randomUUID().toString()
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val repo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        repo.lagreBrukersporsmaal(Brukersporsmaal(
            fnr="2222",
            soknadid =soknadID,
            eventDate = LocalDate.now(),
            ytelse = "SYKEPENGER",
            status="SENDT",
            sporsmaal = FlexBrukerSporsmaal(false)

        ))
        val brukersporsmaal = repo.finnBrukersporsmaalForSoknad(soknadID)
        assertNotNull(brukersporsmaal!!.sporsmaal)
        assertEquals(false,brukersporsmaal.sporsmaal!!.arbeidUtland,"arbeid utland skal være satt til false")
        assertEquals("2222".sha256(),brukersporsmaal.fnr,"fnr er ikke korrekt")

    }
    @Test
    fun `Hente brukersporsmaal basert paa ID som ikke finnes`() {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val soknadID = UUID.randomUUID().toString()
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();

        val repo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        repo.lagreBrukersporsmaal(Brukersporsmaal(
            fnr="2222",
            soknadid =soknadID,
            eventDate = LocalDate.now(),
            ytelse = "SYKEPENGER",
            status="SENDT",
            sporsmaal = FlexBrukerSporsmaal(false)

        ))
        val brukersporsmaal = repo.finnBrukersporsmaalForSoknad("1234")
        assertNull(brukersporsmaal)


    }
    @Test
    fun `lagre brukersporsmaal UTEN flexBrukerSpørsmål`() {
        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val soknadID = UUID.randomUUID().toString()
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();
        dsb.getDataSource().connection.createStatement().execute("delete  from brukersporsmaal")
        val repo = PostgresBrukersporsmaalRepository(dsb.getDataSource())
        repo.lagreBrukersporsmaal(Brukersporsmaal(
            fnr="2222",
            soknadid =soknadID,
            eventDate = LocalDate.now(),
            ytelse = "SYKEPPENGER",
            status="SENDT",
            sporsmaal = FlexBrukerSporsmaal(null)

        ))
        val result = repo.finnBrukersporsmaal("2222")

        assertTrue(result.size==1,"result set should contain 1 element")
        val brukersporsmaal = result.first();
        assertNotNull(brukersporsmaal.sporsmaal)
        assertNull(brukersporsmaal.sporsmaal!!.arbeidUtland,"arbeid utland")
        assertEquals("2222".sha256(),brukersporsmaal.fnr,"fnr er ikke korrekt")

    }
    @Test
    fun `opprettDataSource fra enviroment`() {

        postgresqlContainer.withUrlParam("user", postgresqlContainer.username)
        postgresqlContainer.withUrlParam("password", postgresqlContainer.password)
        val dsb = DataSourceBuilder(mapOf("DB_JDBC_URL" to postgresqlContainer.jdbcUrl))
        dsb.migrate();
        val repo: MedlemskapVurdertRepository = PostgresMedlemskapVurdertRepository(dsb.getDataSource())
        assertNotNull(repo)

    }
}