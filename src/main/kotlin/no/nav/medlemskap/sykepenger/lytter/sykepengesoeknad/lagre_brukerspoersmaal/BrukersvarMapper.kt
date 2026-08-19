package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorgeSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforEOS
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstillatelseBrukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapUtførtArbeidUtenforNorge
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
            spørsmål = mapper.arbeidUtenforNorgeBrukerspørsmål,
            oppholdstilatelse = mapper.oppholdstilatelseBrukerspørsmål,
            utførtArbeidUtenforNorge = mapper.utførtArbeidUtenforNorgeBrukerspørsmål,
            oppholdUtenforNorge = mapper.oppholdUtenforNorgeSpørsmål,
            oppholdUtenforEØS = mapper.oppholdUtenforEØSbrukerspørsmål
        )
    }

    private fun SykepengesoeknadGrunnlag.tilBrukerspørsmålUtenBrukersvar(): Brukersporsmaal =
        tilBrukerspørsmål()

    private fun SykepengesoeknadGrunnlag.tilBrukerspørsmål(
        spørsmål: ArbeidUtenforNorgeSpørsmål? = null,
        oppholdstilatelse: MedlemskapOppholdstillatelseBrukerspørsmål? = null,
        utførtArbeidUtenforNorge: MedlemskapUtførtArbeidUtenforNorge? = null,
        oppholdUtenforNorge: MedlemskapOppholdUtenforNorge? = null,
        oppholdUtenforEØS: MedlemskapOppholdUtenforEOS? = null
    ): Brukersporsmaal {
        return Brukersporsmaal(
            fnr = fnr,
            soknadid = id,
            eventDate = finnTidligsteDato(sendtArbeidsgiver?.toLocalDate(), sendtNav?.toLocalDate()),
            ytelse = "SYKEPENGER",
            status = status,
            sporsmaal = spørsmål,
            oppholdstilatelse = oppholdstilatelse,
            utfortArbeidUtenforNorge = utførtArbeidUtenforNorge,
            oppholdUtenforNorge = oppholdUtenforNorge,
            oppholdUtenforEOS = oppholdUtenforEØS
        )
    }

    private fun finnTidligsteDato(sendArbeidsgiverDato: LocalDate?, sendtNavDato: LocalDate?): LocalDate {
        return listOfNotNull(sendArbeidsgiverDato, sendtNavDato).minOrNull() ?: LocalDate.now()
    }
}
