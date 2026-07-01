package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_eos
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_norge
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_oppholdstilatelse_brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_utfort_arbeid_utenfor_norge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import java.time.LocalDate

object BrukersvarMapper {

    fun mapBrukerspørsmål(sykepengesoeknadGrunnlag: SykepengesoeknadGrunnlag): Brukersporsmaal {
        if (sykepengesoeknadGrunnlag.dodsdato != null) {
            return sykepengesoeknadGrunnlag.tilBrukerspørsmålUtenBrukersvar()
        }

        val mapper = BrukersporsmaalMapper(sykepengesoeknadGrunnlag.sporsmal)
        return sykepengesoeknadGrunnlag.tilBrukerspørsmål(
            sporsmaal = mapper.brukersp_arb_utland_old_model,
            oppholdstilatelse = mapper.oppholdstilatelse_brukersporsmaal,
            utfortArbeidUtenforNorge = mapper.arbeidUtlandBrukerSporsmaal,
            oppholdUtenforNorge = mapper.oppholdUtenforNorge,
            oppholdUtenforEOS = mapper.oppholdUtenforEOS
        )
    }

    private fun SykepengesoeknadGrunnlag.tilBrukerspørsmålUtenBrukersvar(): Brukersporsmaal =
        tilBrukerspørsmål()

    private fun SykepengesoeknadGrunnlag.tilBrukerspørsmål(
        sporsmaal: FlexBrukerSporsmaal? = null,
        oppholdstilatelse: Medlemskap_oppholdstilatelse_brukersporsmaal? = null,
        utfortArbeidUtenforNorge: Medlemskap_utfort_arbeid_utenfor_norge? = null,
        oppholdUtenforNorge: Medlemskap_opphold_utenfor_norge? = null,
        oppholdUtenforEOS: Medlemskap_opphold_utenfor_eos? = null
    ): Brukersporsmaal {
        return Brukersporsmaal(
            fnr = fnr,
            soknadid = id,
            eventDate = finnTidligsteDato(sendtArbeidsgiver?.toLocalDate(), sendtNav?.toLocalDate()),
            ytelse = "SYKEPENGER",
            status = status,
            sporsmaal = sporsmaal,
            oppholdstilatelse = oppholdstilatelse,
            utfort_arbeid_utenfor_norge = utfortArbeidUtenforNorge,
            oppholdUtenforNorge = oppholdUtenforNorge,
            oppholdUtenforEOS = oppholdUtenforEOS
        )
    }

    private fun finnTidligsteDato(sendArbeidsgiverDato: LocalDate?, sendtNavDato: LocalDate?): LocalDate {
        return listOfNotNull(sendArbeidsgiverDato, sendtNavDato).minOrNull() ?: LocalDate.now()
    }
}
