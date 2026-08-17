package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import no.nav.medlemskap.sykepenger.lytter.persistence.sporsmaalSvar

object BrukerSpoersmaalMapperHjelper{
    fun mapSvar(svar: List<sporsmaalSvar>?): Boolean {
        return svar?.first()?.verdi == "JA"
    }

    fun mapBrukerSpoersmaalDato(svar: List<sporsmaalSvar>?): List<Periode> {
        return svar.orEmpty().map { objectMapper.readValue<Periode>(it.verdi) }
    }

}