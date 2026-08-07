package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_eos
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_norge
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_oppholdstilatelse_brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_utfort_arbeid_utenfor_norge
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.service.TidligereBrukersvar
import no.nav.persistence.BrukersporsmaalInMemmoryRepository
import no.nav.persistence.MedlemskapVurdertInMemmoryRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UtledBrukerspørsmålTest {
    private val brukersporsmaalRepository = BrukersporsmaalInMemmoryRepository()
    private val utledBrukerspørsmål = HentGjenbrukbareBrukerspoersmaal(
        TidligereBrukersvar(
            PersistenceService(
                medlemskapVurdertRepository = MedlemskapVurdertInMemmoryRepository(),
                brukersporsmaalRepository = brukersporsmaalRepository
            )
        )
    )

    @Test
    fun `mapper nyeste gjenbrukbare svar til spørsmål`() {
        brukersporsmaalRepository.lagreBrukersporsmaal(
            brukersporsmaal(
                eventDate = LocalDate.parse("2023-08-23"),
                oppholdUtenforEos = false,
                oppholdstillatelse = true
            )
        )

        val spørsmål = utledBrukerspørsmål.finnTidligereStilteSpørsmål(
            medlemskapOppslagRequest(førsteDagForYtelse = "2023-08-30")
        )

        assertThat(spørsmål).containsExactly(
            Spørsmål.ARBEID_UTENFOR_NORGE,
            Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE,
            Spørsmål.OPPHOLDSTILATELSE
        )
    }

    @Test
    fun `ignorerer tidligere svar som ikke er gjenbrukbare`() {
        brukersporsmaalRepository.lagreBrukersporsmaal(
            brukersporsmaal(
                eventDate = LocalDate.parse("2023-08-23"),
                oppholdUtenforNorge = false,
                oppholdstillatelse = false
            )
        )

        val spørsmål = utledBrukerspørsmål.finnTidligereStilteSpørsmål(
            medlemskapOppslagRequest(førsteDagForYtelse = "2023-08-30")
        )

        assertThat(spørsmål).isEmpty()
    }

    private fun medlemskapOppslagRequest(førsteDagForYtelse: String) =
        MedlOppslagRequest(
            fnr = FNR,
            førsteDagForYtelse = førsteDagForYtelse,
            periode = Periode("", ""),
            brukerinput = Brukerinput(arbeidUtenforNorge = false)
        )

    private fun brukersporsmaal(
        eventDate: LocalDate,
        oppholdUtenforNorge: Boolean? = null,
        oppholdUtenforEos: Boolean? = null,
        oppholdstillatelse: Boolean? = null
    ) = Brukersporsmaal(
        fnr = FNR,
        soknadid = "soknad-$eventDate",
        eventDate = eventDate,
        ytelse = "SYKEPENGER",
        status = "SENDT",
        sporsmaal = null,
        oppholdstilatelse = oppholdstillatelse?.let {
            Medlemskap_oppholdstilatelse_brukersporsmaal(
                id = "oppholdstillatelse",
                sporsmalstekst = null,
                svar = it,
                vedtaksdato = LocalDate.parse("2023-01-01"),
                vedtaksTypePermanent = false
            )
        },
        utfort_arbeid_utenfor_norge = Medlemskap_utfort_arbeid_utenfor_norge(
            id = "arbeid-utenfor-norge",
            sporsmalstekst = null,
            svar = false,
            arbeidUtenforNorge = emptyList()
        ),
        oppholdUtenforNorge = oppholdUtenforNorge?.let {
            Medlemskap_opphold_utenfor_norge(
                id = "opphold-utenfor-norge",
                sporsmalstekst = null,
                svar = it,
                oppholdUtenforNorge = emptyList()
            )
        },
        oppholdUtenforEOS = oppholdUtenforEos?.let {
            Medlemskap_opphold_utenfor_eos(
                id = "opphold-utenfor-eos",
                sporsmalstekst = null,
                svar = it,
                oppholdUtenforEOS = emptyList()
            )
        }
    )

    companion object {
        private const val FNR = "12345678910"
    }
}
