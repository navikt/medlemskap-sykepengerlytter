package no.nav.medlemskap.sykepenger.lytter.service

import kotlinx.coroutines.runBlocking

import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadsstatusDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadstypeDTO
import no.nav.medlemskap.sykepenger.lytter.persistence.*
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
        val brukersporsmaal = service.hentNyesteBrukerSporsmaalFromDatabase(sykepengeSoknad)
        Assertions.assertNotNull(brukersporsmaal)
        Assertions.assertTrue(brukersporsmaal.sporsmaal?.arbeidUtland!!)

    }



    @Test
    fun `brukersporsmaal med null verdier skal ikke anses som siste`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo,repo2)

        val dato = LocalDate.of(2025,10,30)

        val sykepengeSoknad = LovmeSoknadDTO(
            id = UUID.randomUUID().toString(),
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            status = SoknadsstatusDTO.SENDT.name,
            fnr = "01010112345",
            korrigerer = null,
            startSyketilfelle = dato,
            sendtNav = dato.atStartOfDay(),
            fom = dato,
            tom = dato.plusDays(20),
            ettersending = null
        )

        val service= SoknadRecordHandler(Configuration(), persistenceService)
        /*
        * legg til to brukerssvar for opphold utenfor EØS der den siste! er null (ikke oppgitt)
        * */
        val eventDate =
        repo2.storage.add(
            Brukersporsmaal(
                fnr = "01010112345",
                soknadid = UUID.randomUUID().toString(),
                eventDate = dato.minusDays(10),
                sporsmaal = FlexBrukerSporsmaal(false),
                oppholdUtenforEOS = Medlemskap_opphold_utenfor_eos(
                    id = UUID.randomUUID().toString(),
                    sporsmalstekst = "",
                    svar=false,
                    oppholdUtenforEOS = emptyList()
                ),
                status = "SENDT",
                ytelse = "SYKEPENGER"
        ))
        repo2.storage.add(
            Brukersporsmaal(
                fnr = "01010112345",
                soknadid = UUID.randomUUID().toString(),
                eventDate = dato.minusDays(5),
                sporsmaal = FlexBrukerSporsmaal(false),
                oppholdUtenforEOS = null,
                status = "SENDT",
                ytelse = "SYKEPENGER"
            ))
        val brukersporsmaal = service.hentNyesteBrukerSporsmaalFromDatabase(sykepengeSoknad)
        Assertions.assertNotNull(brukersporsmaal)
        Assertions.assertFalse(brukersporsmaal.sporsmaal?.arbeidUtland!!)

    }
    @Test
    fun `brukersvar for oppholdstillatelse med svar Ja som er etter ny modell skal gjenbrukes`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val sykepengesoknadID = UUID.randomUUID().toString()

        val startSyketilfelle = LocalDate.of(2025,3,23)
        val datoSendtNav = LocalDateTime.of(2025,3,25,0,0)
        val datoLagretBrukersvar = LocalDate.of(2025,3,25)

        repo2.storage = mutableListOf(
            Brukersporsmaal(
                fnr = "01010112345",
                soknadid = sykepengesoknadID,
                eventDate = datoLagretBrukersvar,
                ytelse = "SYKEPENGER",
                status = "SENDT",
                sporsmaal = null,
                oppholdstilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
                    id="",
                    sporsmalstekst = "",
                    svar = true,
                    vedtaksdato = datoLagretBrukersvar,
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
            startSyketilfelle,
            datoSendtNav,
            LocalDate.of(2025,3,23),
            LocalDate.of(2025,4,8),
            null
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        val brukersporsmaal = service.hentNyesteBrukerSporsmaalFromDatabase(sykepengeSoknad)
        Assertions.assertNotNull(brukersporsmaal)
        Assertions.assertNotNull(brukersporsmaal.oppholdstilatelse)
    }

    @Test
    fun `brukersvar for oppholdstillatelse med svar Ja som er før ny modell skal ikke gjenbrukes`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val sykepengesoknadID = UUID.randomUUID().toString()

        val startSyketilfelle = LocalDate.of(2022,3,23)
        val datoSendtNav = LocalDateTime.of(2022,3,25, 0,0)
        val datoLagretBrukersvar = LocalDate.of(2022,3,25)

        repo2.storage = mutableListOf(
            Brukersporsmaal(
                fnr = "01010112345",
                soknadid = sykepengesoknadID,
                eventDate = datoLagretBrukersvar,
                ytelse = "SYKEPENGER",
                status = "SENDT",
                sporsmaal = null,
                oppholdstilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
                    id="",
                    sporsmalstekst = "",
                    svar = true,
                    vedtaksdato = datoLagretBrukersvar,
                    vedtaksTypePermanent = true,
                    perioder = listOf(Periode(LocalDate.MIN, LocalDate.of(2000,1,1)))
                ),
                utfort_arbeid_utenfor_norge = null,
                oppholdUtenforNorge = null,
                oppholdUtenforEOS = null)
        )
        val persistenceService = PersistenceService(repo,repo2)

        val sykepengeSoknad = LovmeSoknadDTO(UUID.randomUUID().toString(),
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            status = SoknadsstatusDTO.SENDT.name,
            fnr = "01010112345",
            startSyketilfelle = startSyketilfelle,
            sendtNav = datoSendtNav,
            fom = LocalDate.of(2022,3,23),
            tom = LocalDate.of(2022,3,30)
        )

        val service= SoknadRecordHandler(Configuration(), persistenceService)
        val brukersporsmaal = service.hentNyesteBrukerSporsmaalFromDatabase(sykepengeSoknad)
        Assertions.assertNotNull(brukersporsmaal)
        Assertions.assertNull(brukersporsmaal.oppholdstilatelse)
    }

    @Test
    fun `brukersvar for oppholdstillatelse med svar Nei skal ikke gjenbrukes`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val sykepengesoknadID = UUID.randomUUID().toString()

        val startSyketilfelle = LocalDate.of(2025,3,23)
        val datoSendtNav = LocalDateTime.of(2025,3,25,0,0)
        val datoLagretBrukersvar = LocalDate.of(2025,3,25)

        val oppgittNei = false

        repo2.storage = mutableListOf(
            Brukersporsmaal(
                fnr = "01010112345",
                soknadid = sykepengesoknadID,
                eventDate = datoLagretBrukersvar,
                ytelse = "SYKEPENGER",
                status = "SENDT",
                sporsmaal = null,
                oppholdstilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
                    id="",
                    sporsmalstekst = "",
                    svar = oppgittNei,
                    vedtaksdato = datoLagretBrukersvar,
                    vedtaksTypePermanent = false,
                    perioder = emptyList()
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
            startSyketilfelle,
            datoSendtNav,
            LocalDate.of(2025,3,23),
            LocalDate.of(2025,4,8),
            null
        )
        val service= SoknadRecordHandler(Configuration(), persistenceService)
        val brukersporsmaal = service.hentNyesteBrukerSporsmaalFromDatabase(sykepengeSoknad)
        Assertions.assertNull(brukersporsmaal.oppholdstilatelse)
    }

}