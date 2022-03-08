package no.nav.persistence

import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.saga.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.service.SoknadRecordHandler
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
        private val postgresqlContainer     = MyPostgreSQLContainer("postgres:12")
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
        repo.lagreVurdering(VurderingDao("1234","1234", LocalDate.now(), LocalDate.now(),ErMedlem.PAFOLGENDE.toString()))
        val result = repo.finnVurdering("1234")

        assertTrue(result.size==1,"result set should contain 3 elements")

        assertEquals("1234".sha256(),result.first().fnr)
        assertEquals(ErMedlem.PAFOLGENDE,ErMedlem.valueOf(result.first().status))
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