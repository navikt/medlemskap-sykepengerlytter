package no.nav.persistence


import no.nav.medlemskap.sykepenger.lytter.jackson.MedlemskapVurdertParser
import no.nav.medlemskap.sykepenger.lytter.service.VurderingDaoMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class PersistenceServiceMappingTest {

    @Test
    fun `JsonNode skal mappes til DAO objekt`() {
        val soknad_id = UUID.randomUUID().toString()
        val fileContent = this::class.java.classLoader.getResource("sampleVurdering.json").readText(Charsets.UTF_8)
        val JsonNode = MedlemskapVurdertParser().parse(fileContent)
        Assertions.assertNotNull(JsonNode)
        val dao = VurderingDaoMapper().mapJsonNodeToVurderingDao(soknad_id,JsonNode)
        Assertions.assertEquals(soknad_id,dao.id)
        Assertions.assertEquals("12345678901",dao.fnr)
        Assertions.assertEquals(LocalDate.of(2019,3,22),dao.fom)
        Assertions.assertEquals(LocalDate.of(2019,4,8),dao.tom)
        Assertions.assertEquals("JA",dao.status)
    }
}