package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforEOS
import no.nav.medlemskap.sykepenger.lytter.persistence.OppholdUtenforEOS
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapSvar

fun getOppholdUtenforEOSBrukerSporsmaal(
    oppholdUtenforEOSbrukerspoersmaal: MedlemskapsBrukerSpørsmål?
): MedlemskapOppholdUtenforEOS? {
    return if (oppholdUtenforEOSbrukerspoersmaal != null) {
        mapOppholdUtenforEOS_BrukerSporsmaal(oppholdUtenforEOSbrukerspoersmaal)
    } else {
        null
    }
}

private fun mapOppholdUtenforEOS_BrukerSporsmaal(
    oppholdutenforEOS: MedlemskapsBrukerSpørsmål
): MedlemskapOppholdUtenforEOS {
    val svar = mapSvar(oppholdutenforEOS.svar)
    return MedlemskapOppholdUtenforEOS(
        id = oppholdutenforEOS.id,
        sporsmalstekst = oppholdutenforEOS.spørsmålstekst,
        svar = svar,
        oppholdUtenforEOS = if (svar) mapOppholdUtenforEOSunderspoersmaal(oppholdutenforEOS.underspørsmål) else emptyList(),
    )
}

private fun mapOppholdUtenforEOSunderspoersmaal(
    underspoersmaal: List<MedlemskapsBrukerSpørsmål>?
): List<OppholdUtenforEOS> {
    return underspoersmaal.orEmpty()
        .filter { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_GRUPPERING") }
        .map {
            val undersporsmal = it.underspørsmål.orEmpty()

            val oppholdUtenforEOSbegrunnelseSporsmaal = undersporsmal
                .find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_BEGRUNNELSE") }?.underspørsmål

            val oppholdUtenforEOSbegrunnelseSporsmaalstekst =
                oppholdUtenforEOSbegrunnelseSporsmaal?.find { it.svar?.size == 1 }?.spørsmålstekst

            val oppholdutenforEOSspoersmaalHvorVerdi = undersporsmal
                .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_HVOR") }.svar!!.first().verdi

            val oppholdUtenforEOSNaarDato =
                undersporsmal
                    .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_NAAR") }

            OppholdUtenforEOS(
                id = it.id,
                land = oppholdutenforEOSspoersmaalHvorVerdi,
                grunn = oppholdUtenforEOSbegrunnelseSporsmaalstekst ?: "null",
                perioder = mapBrukerSpørsmålDato(oppholdUtenforEOSNaarDato.svar),
            )
        }
}