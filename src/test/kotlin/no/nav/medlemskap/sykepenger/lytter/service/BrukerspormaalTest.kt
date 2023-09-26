package no.nav.medlemskap.sykepenger.lytter.service

import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.saga.persistence.*
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadsstatusDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.persistence.BrukersporsmaalInMemmoryRepository
import no.nav.persistence.MedlemskapVurdertInMemmoryRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class BrukerspormaalTest {
    @Test
    fun `Ingen bruker spørsmål skal returnere et bruker sporsmaal med true i arbeid utland gammel modell`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo,repo2)

        val sykepengeSoknad = LovmeSoknadDTO(UUID.randomUUID().toString(),
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT.name,
            "01010112345",
            null,
            LocalDate.of(2022,3,23),
            LocalDateTime.now(), LocalDate.of(2022,3,23),
            LocalDate.of(2022,4,8),
            null
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        val brukersporsmaal = service.getRelevanteBrukerSporsmaal(sykepengeSoknad)
        Assertions.assertNotNull(brukersporsmaal)
        Assertions.assertTrue(brukersporsmaal.sporsmaal?.arbeidUtland!!)

    }
    @Test
    fun `oppholdstilatelse innenfor med dato innenfor dagens dato skal returneres`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val sykepengesoknadID = UUID.randomUUID().toString()

        repo2.storage = mutableListOf(
            Brukersporsmaal(
                fnr = "01010112345",
                soknadid = sykepengesoknadID,
                eventDate = LocalDate.now(),
                ytelse = "SYKEPENGER",
                status = "SENDT",
                sporsmaal = null,
                oppholdstilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
                    id="",
                    sporsmalstekst = "",
                    svar = true,
                    vedtaksdato = LocalDate.now(),
                    vedtaksTypePermanent = false,
                    perioder = listOf(Periode(LocalDate.MIN, LocalDate.MAX))
                ),
                utfort_arbeid_utenfor_norge = null,
                oppholdUtenforNorge = null,
                oppholdUtenforEOS = null)
        )
        val persistenceService = PersistenceService(repo,repo2)

        val sykepengeSoknad = LovmeSoknadDTO(UUID.randomUUID().toString(),
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT.name,
            "01010112345",
            null,
            LocalDate.of(2022,3,23),
            LocalDateTime.now(), LocalDate.of(2022,3,23),
            LocalDate.of(2022,4,8),
            null
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        val brukersporsmaal = service.getRelevanteBrukerSporsmaal(sykepengeSoknad)
        Assertions.assertNotNull(brukersporsmaal)
        Assertions.assertNotNull(brukersporsmaal.oppholdstilatelse)
    }
    @Test
    fun `oppholdstilatelse utenfordagens dato skal ikke returneres`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val sykepengesoknadID = UUID.randomUUID().toString()

        repo2.storage = mutableListOf(
            Brukersporsmaal(
                fnr = "01010112345",
                soknadid = sykepengesoknadID,
                eventDate = LocalDate.now(),
                ytelse = "SYKEPENGER",
                status = "SENDT",
                sporsmaal = null,
                oppholdstilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
                    id="",
                    sporsmalstekst = "",
                    svar = true,
                    vedtaksdato = LocalDate.now(),
                    vedtaksTypePermanent = false,
                    perioder = listOf(Periode(LocalDate.MIN, LocalDate.of(2000,1,1)))
                ),
                utfort_arbeid_utenfor_norge = null,
                oppholdUtenforNorge = null,
                oppholdUtenforEOS = null)
        )
        val persistenceService = PersistenceService(repo,repo2)

        val sykepengeSoknad = LovmeSoknadDTO(UUID.randomUUID().toString(),
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT.name,
            "01010112345",
            null,
            LocalDate.of(2022,3,23),
            LocalDateTime.now(), LocalDate.of(2022,3,23),
            LocalDate.of(2022,4,8),
            null
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        val brukersporsmaal = service.getRelevanteBrukerSporsmaal(sykepengeSoknad)
        Assertions.assertNotNull(brukersporsmaal)
        Assertions.assertNull(brukersporsmaal.oppholdstilatelse)
    }

}