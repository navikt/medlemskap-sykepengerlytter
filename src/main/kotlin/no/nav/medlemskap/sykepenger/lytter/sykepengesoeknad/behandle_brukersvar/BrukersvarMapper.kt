package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_brukersvar

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadMelding
import java.time.LocalDate

object BrukersvarMapper {
    private val log = KotlinLogging.logger { }

    fun mapMessage(sykepengesoeknadMelding: SykepengesoeknadMelding): Brukersporsmaal =
        mapMessage(JacksonParser().lesSykepengesøknadGrunnlag(sykepengesoeknadMelding.value))

    fun mapMessage(sykepengesoeknadGrunnlag: SykepengesoeknadGrunnlag): Brukersporsmaal {
        try {
            val fnr = sykepengesoeknadGrunnlag.fnr
            val status = sykepengesoeknadGrunnlag.status
            val id = sykepengesoeknadGrunnlag.id
            val sendtNavDato = sykepengesoeknadGrunnlag.sendtNav?.toLocalDate()
            val sendtArbeidsgiverDato = sykepengesoeknadGrunnlag.sendtArbeidsgiver?.toLocalDate()
            //dersom bruker er død er alle brukerspørsmål ikke oppgitt.

            if (sykepengesoeknadGrunnlag.dodsdato != null){
                return Brukersporsmaal(
                    fnr,
                    id,
                    finnTidligsteDato(sendtArbeidsgiverDato, sendtNavDato),
                    "SYKEPENGER",
                    status,
                    null,
                    null,
                    null
                )
            }
            val mapper = BrukersporsmaalMapper(sykepengesoeknadGrunnlag.sporsmal)

            return Brukersporsmaal(
                fnr,
                id,
                finnTidligsteDato(sendtArbeidsgiverDato, sendtNavDato),
                "SYKEPENGER",
                status,
                mapper.brukersp_arb_utland_old_model,
                mapper.oppholdstilatelse_brukersporsmaal,
                mapper.arbeidUtlandBrukerSporsmaal,
                mapper.oppholdUtenforNorge,
                mapper.oppholdUtenforEOS
            )
        }
        catch (t:Throwable){
            log.error("not able to parse message ${t.message}, cause : ${t.cause}")
            throw t
        }
    }

    private fun finnTidligsteDato(sendArbeidsgiverDato: LocalDate?, sendtNavDato: LocalDate?): LocalDate {
        return listOfNotNull(sendArbeidsgiverDato, sendtNavDato).minOrNull() ?: LocalDate.now()
    }
}
