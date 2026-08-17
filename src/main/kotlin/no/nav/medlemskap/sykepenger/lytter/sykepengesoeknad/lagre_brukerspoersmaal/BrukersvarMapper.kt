package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforEOS
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstilatelseBrukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapUtfortArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukersporsmaalMapper
import java.time.LocalDate

object BrukersvarMapper {

    fun tilBrukerspørsmål(sykepengesoeknadGrunnlag: SykepengesoeknadGrunnlag): Brukersporsmaal {
        if (sykepengesoeknadGrunnlag.dodsdato != null) {
            return sykepengesoeknadGrunnlag.tilBrukerspørsmålUtenBrukersvar()
        }

        val mapper = BrukersporsmaalMapper(sykepengesoeknadGrunnlag.sporsmal)
        return sykepengesoeknadGrunnlag.tilBrukerspørsmål(
            sporsmaal = mapper.brukersporsmaalArbeidUtlandOldModel,
            oppholdstilatelse = mapper.oppholdstilatelseBrukersporsmaal,
            utfortArbeidUtenforNorge = mapper.arbeidUtlandBrukerSporsmaal,
            oppholdUtenforNorge = mapper.oppholdUtenforNorge,
            oppholdUtenforEOS = mapper.oppholdUtenforEOS
        )
    }

    private fun SykepengesoeknadGrunnlag.tilBrukerspørsmålUtenBrukersvar(): Brukersporsmaal =
        tilBrukerspørsmål()

    private fun SykepengesoeknadGrunnlag.tilBrukerspørsmål(
        sporsmaal: FlexBrukerSporsmaal? = null,
        oppholdstilatelse: MedlemskapOppholdstilatelseBrukersporsmaal? = null,
        utfortArbeidUtenforNorge: MedlemskapUtfortArbeidUtenforNorge? = null,
        oppholdUtenforNorge: MedlemskapOppholdUtenforNorge? = null,
        oppholdUtenforEOS: MedlemskapOppholdUtenforEOS? = null
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
