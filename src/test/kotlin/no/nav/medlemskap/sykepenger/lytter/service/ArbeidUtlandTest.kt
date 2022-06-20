package no.nav.medlemskap.sykepenger.lytter.service

import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.saga.persistence.Brukersporsmaal
import no.nav.medlemskap.saga.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.saga.persistence.VurderingDao
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

class ArbeidUtlandTest {
    @Test
    fun `Ingen data i databasen og null i arbeid utland skal svare true`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo,repo2)

        val sykepengeSoknad = LovmeSoknadDTO(UUID.randomUUID().toString(),
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            "01010112345",
            null,
            LocalDate.of(2022,3,23),
            LocalDateTime.now(), LocalDate.of(2022,3,23),
            LocalDate.of(2022,4,8),
            null
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        Assertions.assertTrue(service.getArbeidUtlandFromBrukerSporsmaal(sykepengeSoknad))
    }
    @Test
    fun `data i databasen eldre en 1 Ar skal ignoreres`() = runBlocking {
        val fnrInUseCase = "01010112345"
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo,repo2)
        repo2.lagreBrukersporsmaal(Brukersporsmaal
            (fnrInUseCase,
            UUID.randomUUID().toString(),
            LocalDate.now().minusYears(1),
            "SYKEPENGER","SENDT",
            FlexBrukerSporsmaal(false)
            )
        )
        val sykepengeSoknad = LovmeSoknadDTO(UUID.randomUUID().toString(),
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            fnrInUseCase,
            null,
            LocalDate.of(2022,3,23),
            LocalDateTime.now(), LocalDate.of(2022,3,23),
            LocalDate.of(2022,4,8),
            null
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        Assertions.assertTrue(service.getArbeidUtlandFromBrukerSporsmaal(sykepengeSoknad))
    }
    @Test
    fun `data i databasen mindre en 1 Ar skal brukes - Nei oppgitt`() = runBlocking {
        val fnrInUseCase = "01010112345"
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo,repo2)
        repo2.lagreBrukersporsmaal(Brukersporsmaal
            (fnrInUseCase,
            UUID.randomUUID().toString(),
            LocalDate.now().minusMonths(11),
            "SYKEPENGER","SENDT",
            FlexBrukerSporsmaal(false)
        )
        )
        val sykepengeSoknad = LovmeSoknadDTO(UUID.randomUUID().toString(),
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            fnrInUseCase,
            null,
            LocalDate.of(2022,3,23),
            LocalDateTime.now(), LocalDate.of(2022,3,23),
            LocalDate.of(2022,4,8),
            null
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        Assertions.assertFalse(service.getArbeidUtlandFromBrukerSporsmaal(sykepengeSoknad))
    }
    @Test
    fun `true i arbeid utland paa soknad skal alltid returnere true`() = runBlocking {
        val fnrInUseCase = "01010112345"
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo,repo2)
        repo2.lagreBrukersporsmaal(Brukersporsmaal
            (fnrInUseCase,
            UUID.randomUUID().toString(),
            LocalDate.now().minusMonths(11),
            "SYKEPENGER","SENDT",
            FlexBrukerSporsmaal(false)
        )
        )
        val sykepengeSoknad = LovmeSoknadDTO(UUID.randomUUID().toString(),
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            fnrInUseCase,
            null,
            LocalDate.of(2022,3,23),
            LocalDateTime.now(), LocalDate.of(2022,3,23),
            LocalDate.of(2022,4,8),
            true,
            arbeidUtenforNorge = true
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        Assertions.assertTrue(service.getArbeidUtlandFromBrukerSporsmaal(sykepengeSoknad))
    }
    @Test
    fun `data i databasen mindre en 1 Ar skal brukes - Ja oppgitt`() = runBlocking {
        val fnrInUseCase = "01010112345"
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo,repo2)
        repo2.lagreBrukersporsmaal(Brukersporsmaal
            (fnrInUseCase,
            UUID.randomUUID().toString(),
            LocalDate.now().minusMonths(11),
            "SYKEPENGER","SENDT",
            FlexBrukerSporsmaal(true)
            )
        )
        repo2.lagreBrukersporsmaal(Brukersporsmaal
            (fnrInUseCase,
            UUID.randomUUID().toString(),
            LocalDate.now().minusMonths(10),
            "SYKEPENGER","SENDT",
            FlexBrukerSporsmaal(null)
        )
        )
        repo2.lagreBrukersporsmaal(Brukersporsmaal
            (fnrInUseCase,
            UUID.randomUUID().toString(),
            LocalDate.now().minusMonths(9),
            "SYKEPENGER","SENDT",
            FlexBrukerSporsmaal(null)
        )
        )
        val sykepengeSoknad = LovmeSoknadDTO(UUID.randomUUID().toString(),
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT,
            fnrInUseCase,
            null,
            LocalDate.of(2022,3,23),
            LocalDateTime.now(), LocalDate.of(2022,3,23),
            LocalDate.of(2022,4,8),
            null
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        Assertions.assertTrue(service.getArbeidUtlandFromBrukerSporsmaal(sykepengeSoknad))
    }
}