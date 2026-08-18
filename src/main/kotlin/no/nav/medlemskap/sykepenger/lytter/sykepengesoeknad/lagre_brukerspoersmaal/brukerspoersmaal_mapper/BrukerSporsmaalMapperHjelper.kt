package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import no.nav.medlemskap.sykepenger.lytter.persistence.spørsmålSvar

object BrukerSporsmaalMapperHjelper{
    fun mapSvar(svar: List<spørsmålSvar>?): Boolean {
        return svar?.first()?.verdi == "JA"
    }

    fun mapBrukerSpørsmålDato(svar: List<spørsmålSvar>?): List<Periode> {
        return svar.orEmpty().map { objectMapper.readValue<Periode>(it.verdi) }
    }

}