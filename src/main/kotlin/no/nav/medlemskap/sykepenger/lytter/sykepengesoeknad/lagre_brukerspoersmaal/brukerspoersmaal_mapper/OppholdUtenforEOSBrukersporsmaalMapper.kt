package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_eos
import no.nav.medlemskap.sykepenger.lytter.persistence.OppholdUtenforEOS
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapBrukerSpoersmaalNaarDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapSvar

fun getOppholdUtenforEOSBrukerSporsmaal(
    oppholdUtenforEOSbrukerspoersmaal: FlexMedlemskapsBrukerSporsmaal?
): Medlemskap_opphold_utenfor_eos? {
    if (oppholdUtenforEOSbrukerspoersmaal != null) {
        return mapOppholdUtenforEOS_BrukerSporsmaal(oppholdUtenforEOSbrukerspoersmaal)
    }
    return null
}

private fun mapOppholdUtenforEOS_BrukerSporsmaal(
    oppholdutenforEOS: FlexMedlemskapsBrukerSporsmaal
): Medlemskap_opphold_utenfor_eos {
    val svar = mapSvar(oppholdutenforEOS.svar)
    return Medlemskap_opphold_utenfor_eos(
        id = oppholdutenforEOS.id,
        sporsmalstekst = oppholdutenforEOS.sporsmalstekst,
        svar = svar,
        oppholdUtenforEOS = if (svar) mapOppholdUtenforEOSunderspoersmaal(oppholdutenforEOS.undersporsmal) else emptyList(),
    )
}

private fun mapOppholdUtenforEOSunderspoersmaal(
    underspoersmaal: List<FlexMedlemskapsBrukerSporsmaal>?
): List<OppholdUtenforEOS> {
    return underspoersmaal?.map {
        val oppholdUtenforEOSspoersmaalGruppering =
            underspoersmaal.first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_GRUPPERING") }

        val oppholdUtenforEOSbegrunnelse = oppholdUtenforEOSspoersmaalGruppering
            .undersporsmal
            ?.first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_BEGRUNNELSE") && it.svar?.size == 1 }

        val oppholdutenforEOSspoersmaalHvorVerdi = oppholdUtenforEOSspoersmaalGruppering
            .undersporsmal
            ?.find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_HVOR") }?.svar!!.first().verdi

        val oppholdUtenforEOSNaarDato =
            oppholdUtenforEOSspoersmaalGruppering.undersporsmal
                .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_NAAR") }

        OppholdUtenforEOS(
            id = it.id,
            land = oppholdutenforEOSspoersmaalHvorVerdi,
            grunn = oppholdUtenforEOSbegrunnelse?.sporsmalstekst ?: "null",
            perioder = mapBrukerSpoersmaalNaarDato(oppholdUtenforEOSNaarDato.svar),
        )
    } ?: emptyList()
}