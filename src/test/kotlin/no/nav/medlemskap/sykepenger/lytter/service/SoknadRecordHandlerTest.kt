package no.nav.medlemskap.sykepenger.lytter.service

import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.*
import no.nav.persistence.BrukersporsmaalInMemmoryRepository
import no.nav.persistence.MedlemskapVurdertInMemmoryRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class SoknadRecordHandlerTest {

    @Test
    fun `test Duplikat på forespørsel`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "123",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 10),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "123",
                LocalDate.of(2022, 1, 11),
                LocalDate.of(2022, 1, 20),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "234",
                LocalDate.of(2022, 1, 21),
                LocalDate.of(2022, 1, 25),
                ErMedlem.JA.toString()
            )
        )
        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val duplikat = service.isDuplikat(
            Medlemskap(
                "123",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 10), ErMedlem.UAVKLART
            )
        )
        Assertions.assertNotNull(duplikat)
    }

    @Test
    fun `test ikke Duplikat på forespørsel`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "123",
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 1, 10),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "123",
                LocalDate.of(2022, 1, 11),
                LocalDate.of(2022, 1, 20),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "234",
                LocalDate.of(2022, 1, 21),
                LocalDate.of(2022, 1, 25),
                ErMedlem.JA.toString()
            )
        )
        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val duplikat = service.isDuplikat(
            Medlemskap(
                "123",
                LocalDate.of(2022, 1, 21),
                LocalDate.of(2022, 1, 25), ErMedlem.UAVKLART
            )
        )
        Assertions.assertNull(duplikat)
    }


    @Test
    fun `test påfølgende forespørsel`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)

        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 8, 25),
                LocalDate.of(2021, 8, 31),
                ErMedlem.JA.toString()
            )
        )

        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        val paafolgende = service.isPaafolgendeSoknad(sykepengeSoknad)
        Assertions.assertTrue(paafolgende)
        val dbResult = repo.finnVurdering("01010112345")
        val paafolgendeMedlemskap = dbResult.find { it.status == "PAFOLGENDE" }
        Assertions.assertNotNull(paafolgendeMedlemskap)
        Assertions.assertEquals(paafolgendeMedlemskap!!.fom, sykepengeSoknad.fom)
        Assertions.assertEquals(paafolgendeMedlemskap!!.tom, sykepengeSoknad.tom)
        Assertions.assertEquals(paafolgendeMedlemskap!!.fnr, sykepengeSoknad.fnr)
        Assertions.assertEquals(paafolgendeMedlemskap!!.id, sykepengeSoknad.id)
    }

    @Test
    fun `test påfølgende forespørsel med mange db innslag`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)

        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 10, 19),
                LocalDate.of(2021, 10, 25),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 10, 5),
                LocalDate.of(2021, 10, 18),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 10, 26),
                LocalDate.of(2021, 11, 24),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 11, 25),
                LocalDate.of(2021, 12, 6),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 12, 7),
                LocalDate.of(2022, 1, 5),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 1, 6),
                LocalDate.of(2022, 1, 22),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 1, 23),
                LocalDate.of(2022, 2, 7),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 2, 8),
                LocalDate.of(2022, 3, 4),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 3, 5),
                LocalDate.of(2022, 3, 22),
                ErMedlem.JA.toString()
            )
        )


        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val sykepengeSoknad = LovmeSoknadDTO(
            id = UUID.randomUUID().toString(),
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            status = SoknadsstatusDTO.SENDT.name,
            fnr = "01010112345",
            korrigerer = null,
            startSyketilfelle = LocalDate.of(2022, 3, 23),
            sendtNav = LocalDateTime.now(),
            fom = LocalDate.of(2022, 3, 23),
            tom = LocalDate.of(2022, 4, 8),
            arbeidUtenforNorge = false
        )
        val paafolgende = service.isPaafolgendeSoknad(sykepengeSoknad)
        Assertions.assertTrue(paafolgende)
    }


    @Test
    fun `test paafolgende foresporsel med mange db innslag2`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)

        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 3, 4),
                LocalDate.of(2022, 3, 21),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 4, 9),
                LocalDate.of(2022, 4, 26),
                ErMedlem.JA.toString()
            )
        )


        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val sykepengeSoknad = LovmeSoknadDTO(
            id = UUID.randomUUID().toString(),
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            status = SoknadsstatusDTO.SENDT.name,
            fnr = "01010112345",
            korrigerer = null,
            startSyketilfelle = LocalDate.of(2022, 4, 27),
            sendtNav = LocalDateTime.now(),
            fom = LocalDate.of(2022, 4, 27),
            tom = LocalDate.of(2022, 5, 13),
            arbeidUtenforNorge = null
        )
        val paafolgende = service.isPaafolgendeSoknad(sykepengeSoknad)
        Assertions.assertTrue(paafolgende)
    }

    @Test
    fun `test paafolgende foresporsel med mange db innslag og forstegangssoknad = true`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)

        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 3, 4),
                LocalDate.of(2022, 3, 21),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 4, 9),
                LocalDate.of(2022, 4, 26),
                ErMedlem.JA.toString()
            )
        )


        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val sykepengeSoknad = LovmeSoknadDTO(
            id = UUID.randomUUID().toString(),
            type = SoknadstypeDTO.ARBEIDSTAKERE,
            status = SoknadsstatusDTO.SENDT.name,
            fnr = "01010112345",
            korrigerer = null,
            startSyketilfelle = LocalDate.of(2022, 4, 27),
            sendtNav = LocalDateTime.now(),
            fom = LocalDate.of(2022, 4, 27),
            tom = LocalDate.of(2022, 5, 13),
            arbeidUtenforNorge = null,
            forstegangssoknad = true
        )
        val paafolgende = service.isPaafolgendeSoknad(sykepengeSoknad)
        Assertions.assertFalse(paafolgende)
    }


    @Test
    fun `test påfølgende forespørsel med mange db innslag og arbeidUtland=true`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)

        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 10, 19),
                LocalDate.of(2021, 10, 25),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 10, 5),
                LocalDate.of(2021, 10, 18),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 10, 26),
                LocalDate.of(2021, 11, 24),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 11, 25),
                LocalDate.of(2021, 12, 6),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 12, 7),
                LocalDate.of(2022, 1, 5),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 1, 6),
                LocalDate.of(2022, 1, 22),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 1, 23),
                LocalDate.of(2022, 2, 7),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 2, 8),
                LocalDate.of(2022, 3, 4),
                ErMedlem.JA.toString()
            )
        )
        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2022, 3, 5),
                LocalDate.of(2022, 3, 22),
                ErMedlem.JA.toString()
            )
        )


        val service = SoknadRecordHandler(Configuration(), persistenceService)
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = LovmeSoknadDTO(
            UUID.randomUUID().toString(),
            SoknadstypeDTO.ARBEIDSTAKERE,
            SoknadsstatusDTO.SENDT.name,
            "01010112345",
            null,
            LocalDate.of(2022, 3, 23),
            LocalDateTime.now(), LocalDate.of(2022, 3, 23),
            LocalDate.of(2022, 4, 8),
            true,
            arbeidUtenforNorge = true
        )
        val paafolgende = service.isPaafolgendeSoknad(sykepengeSoknad)
        Assertions.assertFalse(paafolgende)
    }

    @Test
    fun `test påfølgende forespørsel via handle`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)
        repo.storage.clear()
        repo2.storage.clear()

        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 8, 25),
                LocalDate.of(2021, 8, 31),
                ErMedlem.JA.toString()
            )
        )

        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        service.handle(SoknadRecord(1, 1, "", "", "", sykepengeSoknad))
        val dbResult = repo.finnVurdering("01010112345")
        val paafolgendeMedlemskap = dbResult.find { it.status == "PAFOLGENDE" }
        Assertions.assertNotNull(paafolgendeMedlemskap)
        Assertions.assertEquals(paafolgendeMedlemskap!!.fom, sykepengeSoknad.fom)
        Assertions.assertEquals(paafolgendeMedlemskap!!.tom, sykepengeSoknad.tom)
        Assertions.assertEquals(paafolgendeMedlemskap!!.fnr, sykepengeSoknad.fnr)
        Assertions.assertEquals(paafolgendeMedlemskap!!.id, sykepengeSoknad.id)
    }

    @Test
    fun `test påfølgende forespørsel med utlandLik true`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)
        repo.storage.clear()
        repo2.storage.clear()


        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 8, 25),
                LocalDate.of(2021, 8, 31),
                ErMedlem.JA.toString()
            )
        )

        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        val fileContent =
            this::class.java.classLoader.getResource("sampleRequestUtlandTrue.json").readText(Charsets.UTF_8)
        var sykepengeSoknad = JacksonParser().parse(fileContent)
        service.isPaafolgendeSoknad(sykepengeSoknad)
        val dbResult = repo.finnVurdering("01010112345")
        val paafolgendeMedlemskap = dbResult.find { it.status == "PAFOLGENDE" }

        Assertions.assertNull(paafolgendeMedlemskap)
    }

    @Test
    fun `test duplikat forespørsel via handle`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)

        repo.lagreVurdering(
            VurderingDao(
                UUID.randomUUID().toString(), "01010112345",
                LocalDate.of(2021, 9, 1),
                LocalDate.of(2021, 9, 30),
                ErMedlem.JA.toString()
            )
        )

        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        val fileContent = this::class.java.classLoader.getResource("sampleRequest.json").readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        service.handle(SoknadRecord(1, 1, "", "", "", sykepengeSoknad))
        val dbResult = repo.finnVurdering("01010112345")
        Assertions.assertEquals(1, dbResult.size)

    }

    @Test
    fun `test2 duplikat forespørsel via handle`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)
        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        service.medlOppslagClient = LovMeMock()
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT_AND_SENDT_'NAV.json")
            .readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        service.handle(SoknadRecord(1, 1, "", "", "", sykepengeSoknad))
        service.handle(SoknadRecord(1, 1, "", "", "", sykepengeSoknad))
        val dbResult = repo.finnVurdering("12345678901")
        Assertions.assertEquals(1, dbResult.size)

    }

    @Test
    fun `test førstegangs soeknad med kall mot lovme inneholder bruker spørsmaal`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)
        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        val mock = LovMeMock()
        service.medlOppslagClient = mock
        repo2.lagreBrukersporsmaal(
            Brukersporsmaal(
                fnr = "12345678901",
                soknadid = "52041604-a94a-38ca-b7a6-3e913b5207fa",
                eventDate = LocalDate.now(),
                ytelse = "SYKEPENGER",
                status = "SENDT",
                sporsmaal = null,
                oppholdstilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
                    id = "123",
                    sporsmalstekst = "abc",
                    svar = true,
                    vedtaksdato = LocalDate.now(),
                    vedtaksTypePermanent = true,
                    perioder = emptyList()
                ),
                utfort_arbeid_utenfor_norge = Medlemskap_utfort_arbeid_utenfor_norge(
                    id = "123",
                    sporsmalstekst = "test",
                    svar = false,
                    arbeidUtenforNorge = emptyList()
                ),
                oppholdUtenforNorge = Medlemskap_opphold_utenfor_norge(
                    id = "123",
                    svar = false,
                    sporsmalstekst = "abc",
                    oppholdUtenforNorge = emptyList()

                ),
                oppholdUtenforEOS = Medlemskap_opphold_utenfor_eos(
                    id = "123",
                    sporsmalstekst = "abc",
                    svar = false,
                    oppholdUtenforEOS = emptyList()
                )
            )
        )
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT_AND_SENDT_'NAV.json")
            .readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        service.handle(SoknadRecord(1, 1, "", "", "", sykepengeSoknad))
        val dbResult = repo.finnVurdering("12345678901")
        Assertions.assertEquals(1, dbResult.size)
        Assertions.assertNotNull(mock.request)
        Assertions.assertNotNull(mock.request!!.brukerinput.oppholdstilatelse)
        Assertions.assertNotNull(mock.request!!.brukerinput.arbeidUtenforNorge)
        Assertions.assertNotNull(mock.request!!.brukerinput.oppholdUtenforNorge)
        Assertions.assertNotNull(mock.request!!.brukerinput.oppholdUtenforEos)
    }

    @Test
    fun `test førstegangs soeknad med kall mot lovme inneholder bruker spørsmaal der gammel modell blir overstyrt`() =
        runBlocking {
            val repo = MedlemskapVurdertInMemmoryRepository()
            val repo2 = BrukersporsmaalInMemmoryRepository()
            val persistenceService = PersistenceService(repo, repo2)
            val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
            val mock = LovMeMock()
            service.medlOppslagClient = mock
            repo2.lagreBrukersporsmaal(
                Brukersporsmaal(
                    fnr = "12345678901",
                    soknadid = "52041604-a94a-38ca-b7a6-3e913b5207fa",
                    eventDate = LocalDate.now(),
                    ytelse = "SYKEPENGER",
                    status = "SENDT",
                    sporsmaal = null,
                    oppholdstilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
                        id = "123",
                        sporsmalstekst = "abc",
                        svar = true,
                        vedtaksdato = LocalDate.now(),
                        vedtaksTypePermanent = true,
                        perioder = emptyList()
                    ),
                    utfort_arbeid_utenfor_norge = Medlemskap_utfort_arbeid_utenfor_norge(
                        id = "123",
                        sporsmalstekst = "test",
                        svar = true,
                        arbeidUtenforNorge = emptyList()
                    ),
                    oppholdUtenforNorge = Medlemskap_opphold_utenfor_norge(
                        id = "123",
                        svar = false,
                        sporsmalstekst = "abc",
                        oppholdUtenforNorge = emptyList()

                    ),
                    oppholdUtenforEOS = Medlemskap_opphold_utenfor_eos(
                        id = "123",
                        sporsmalstekst = "abc",
                        svar = false,
                        oppholdUtenforEOS = emptyList()
                    )
                )
            )
            val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT_AND_SENDT_'NAV.json")
                .readText(Charsets.UTF_8)
            val sykepengeSoknad = JacksonParser().parse(fileContent)
            service.handle(SoknadRecord(1, 1, "", "", "", sykepengeSoknad))
            val dbResult = repo.finnVurdering("12345678901")
            Assertions.assertEquals(1, dbResult.size)
            Assertions.assertNotNull(mock.request)
            Assertions.assertTrue(mock.request!!.brukerinput.arbeidUtenforNorge)
            Assertions.assertNotNull(mock.request!!.brukerinput.oppholdstilatelse)
            Assertions.assertNotNull(mock.request!!.brukerinput.arbeidUtenforNorge)
            Assertions.assertNotNull(mock.request!!.brukerinput.oppholdUtenforNorge)
            Assertions.assertNotNull(mock.request!!.brukerinput.oppholdUtenforEos)
        }

    @Test
    fun `søknad kommer inn med brukersvar`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)
        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        val mock = LovMeMock()
        service.medlOppslagClient = mock
        val dato = LocalDate.of(2025, 1, 1)
        repo2.lagreBrukersporsmaal(
            Brukersporsmaal(
                fnr = "12345678901",
                soknadid = "52041604-a94a-38ca-b7a6-3e913b5207fa",
                eventDate = dato,
                ytelse = "SYKEPENGER",
                status = "SENDT",
                sporsmaal = null,
                oppholdstilatelse = Medlemskap_oppholdstilatelse_brukersporsmaal(
                    id = "123",
                    sporsmalstekst = "abc",
                    svar = true,
                    vedtaksdato = dato,
                    vedtaksTypePermanent = true,
                    perioder = emptyList()
                ),
                utfort_arbeid_utenfor_norge = Medlemskap_utfort_arbeid_utenfor_norge(
                    id = "123",
                    sporsmalstekst = "test",
                    svar = false,
                    arbeidUtenforNorge = emptyList()
                ),
                oppholdUtenforNorge = Medlemskap_opphold_utenfor_norge(
                    id = "123",
                    svar = false,
                    sporsmalstekst = "abc",
                    oppholdUtenforNorge = emptyList()

                ),
                oppholdUtenforEOS = Medlemskap_opphold_utenfor_eos(
                    id = "123",
                    sporsmalstekst = "abc",
                    svar = false,
                    oppholdUtenforEOS = emptyList()
                )
            )
        )
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT_AND_SENDT_'NAV.json")
            .readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        service.handle(SoknadRecord(1, 1, "", "", "", sykepengeSoknad))
        val dbResult = repo.finnVurdering("12345678901")
        Assertions.assertEquals(1, dbResult.size)
        Assertions.assertNotNull(mock.request)
        val gjenbruk = BrukersvarGjenbruk()
        val forventetBrukerinput = gjenbruk.skalKanskjeGjenbrukes("52041604-a94a-38ca-b7a6-3e913b5207fa", persistenceService)
        val faktiskBrukerinput = mock.request?.brukerinput
        Assertions.assertEquals(forventetBrukerinput, faktiskBrukerinput)
    }

    @Test
    fun `søknad kommer inn med gammelt brukersvar`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)
        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        val mock = LovMeMock()
        service.medlOppslagClient = mock
        val dato = LocalDate.of(2025, 1, 1)
        repo2.lagreBrukersporsmaal(
            Brukersporsmaal(
                fnr = "12345678901",
                soknadid = "52041604-a94a-38ca-b7a6-3e913b5207fa",
                eventDate = dato,
                ytelse = "SYKEPENGER",
                status = "SENDT",
                sporsmaal = FlexBrukerSporsmaal(true),
                oppholdstilatelse = null,
                utfort_arbeid_utenfor_norge = null,
                oppholdUtenforNorge = null,
                oppholdUtenforEOS = null

            )
        )
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT_AND_SENDT_'NAV.json")
            .readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        service.handle(SoknadRecord(1, 1, "", "", "", sykepengeSoknad))
        val dbResult = repo.finnVurdering("12345678901")
        Assertions.assertEquals(1, dbResult.size)
        Assertions.assertNotNull(mock.request)
        val gjenbruk = BrukersvarGjenbruk()
        val forventetBrukerinput = gjenbruk.skalKanskjeGjenbrukes("52041604-a94a-38ca-b7a6-3e913b5207fa", persistenceService)
        val faktiskBrukerinput = mock.request?.brukerinput
        Assertions.assertEquals(forventetBrukerinput, faktiskBrukerinput)
        Assertions.assertEquals(mock.request?.brukerinput?.utfortAarbeidUtenforNorge, null)
        Assertions.assertEquals(mock.request?.brukerinput?.oppholdstilatelse, null)
        Assertions.assertEquals(mock.request?.brukerinput?.oppholdUtenforEos, null)
        Assertions.assertEquals(mock.request?.brukerinput?.oppholdUtenforNorge, null)
    }

    @Test
    fun `søknad kommer inn med gammelt brukersvar NEI`() = runBlocking {
        val repo = MedlemskapVurdertInMemmoryRepository()
        val repo2 = BrukersporsmaalInMemmoryRepository()
        val persistenceService = PersistenceService(repo, repo2)
        val service: SoknadRecordHandler = SoknadRecordHandler(Configuration(), persistenceService)
        val mock = LovMeMock()
        service.medlOppslagClient = mock
        val dato = LocalDate.of(2025, 1, 1)
        repo2.lagreBrukersporsmaal(
            Brukersporsmaal(
                fnr = "12345678901",
                soknadid = "52041604-a94a-38ca-b7a6-3e913b5207fa",
                eventDate = dato,
                ytelse = "SYKEPENGER",
                status = "SENDT",
                sporsmaal = FlexBrukerSporsmaal(false),
                oppholdstilatelse = null,
                utfort_arbeid_utenfor_norge = null,
                oppholdUtenforNorge = null,
                oppholdUtenforEOS = null

            )
        )
        val fileContent = this::class.java.classLoader.getResource("FlexSampleMessageSENDT_AND_SENDT_'NAV.json")
            .readText(Charsets.UTF_8)
        val sykepengeSoknad = JacksonParser().parse(fileContent)
        service.handle(SoknadRecord(1, 1, "", "", "", sykepengeSoknad))
        val dbResult = repo.finnVurdering("12345678901")
        Assertions.assertEquals(1, dbResult.size)
        Assertions.assertNotNull(mock.request)
        val gjenbruk = BrukersvarGjenbruk()
        val forventetBrukerinput = gjenbruk.skalKanskjeGjenbrukes("52041604-a94a-38ca-b7a6-3e913b5207fa", persistenceService)
        val faktiskBrukerinput = mock.request?.brukerinput
        Assertions.assertEquals(forventetBrukerinput, faktiskBrukerinput)
        Assertions.assertEquals(mock.request?.brukerinput?.utfortAarbeidUtenforNorge, null)
        Assertions.assertEquals(mock.request?.brukerinput?.oppholdstilatelse, null)
        Assertions.assertEquals(mock.request?.brukerinput?.oppholdUtenforEos, null)
        Assertions.assertEquals(mock.request?.brukerinput?.oppholdUtenforNorge, null)
    }
}
/*
* merk at det ikke er likhet i fnr i flex request og simulert lovme response.
* Eneste som sjekkes i dette scenario er at kall mot lovme(Mock sådan)  faktisk utføres'
* */
public class LovMeMock():LovmeAPI {
    var request:MedlOppslagRequest? = null
    override suspend fun vurderMedlemskap(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        request = medlOppslagRequest
        val fileContent = this::class.java.classLoader.getResource("sampleVurdering.json").readText(Charsets.UTF_8)
        return fileContent
    }

    override suspend fun vurderMedlemskapBomlo(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        request = medlOppslagRequest
        val fileContent = this::class.java.classLoader.getResource("sampleVurdering.json").readText(Charsets.UTF_8)
        return fileContent
    }

    override suspend fun brukerspørsmål(medlOppslagRequest: MedlOppslagRequest, callId: String): String {
        request = medlOppslagRequest
        val fileContent = this::class.java.classLoader.getResource("vurdering_eos_borger_uavklart_REGEL_3.json").readText(Charsets.UTF_8)
        return fileContent
    }

}