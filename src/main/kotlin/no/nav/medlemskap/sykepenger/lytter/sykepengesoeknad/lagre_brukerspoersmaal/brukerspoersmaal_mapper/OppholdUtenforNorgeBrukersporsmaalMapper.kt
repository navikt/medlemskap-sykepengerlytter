package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_norge
import no.nav.medlemskap.sykepenger.lytter.persistence.OppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapBrukerSpoersmaalNaarDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapSvar

fun getOppholdUtenforNorgeBrukerSporsmaal(
    oppholdUtenforNorgebrukerspoersmaal: FlexMedlemskapsBrukerSporsmaal?
): Medlemskap_opphold_utenfor_norge? {
    return if (oppholdUtenforNorgebrukerspoersmaal != null) {
        mapOppholdUtenforNorge_BrukerSporsmaal(oppholdUtenforNorgebrukerspoersmaal)
    } else {
        null
    }
}

private fun mapOppholdUtenforNorge_BrukerSporsmaal(
    oppholdUtenforNorge: FlexMedlemskapsBrukerSporsmaal,
): Medlemskap_opphold_utenfor_norge {
    val svar = mapSvar(oppholdUtenforNorge.svar)
    return Medlemskap_opphold_utenfor_norge(
        id = oppholdUtenforNorge.id,
        sporsmalstekst = oppholdUtenforNorge.sporsmalstekst,
        svar = svar,
        oppholdUtenforNorge = if (svar) mapOppholdUtenforNorgeUnderspoersmaal(oppholdUtenforNorge.undersporsmal) else emptyList()
    )
}

private fun mapOppholdUtenforNorgeUnderspoersmaal(
    underspoersmaal: List<FlexMedlemskapsBrukerSporsmaal>?
): List<OppholdUtenforNorge> {
    return underspoersmaal?.map {
        val oppholdUtenforNorgeSpoersmaalGruppering =
            underspoersmaal.first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_GRUPPERING") }

        val oppholdUtenforNorgeBegrunnelse = oppholdUtenforNorgeSpoersmaalGruppering
            .undersporsmal
            ?.first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_BEGRUNNELSE") && it.svar?.size == 1 }

        val oppholdUtenforNorgeHvorVerdi = oppholdUtenforNorgeSpoersmaalGruppering
            .undersporsmal
            ?.find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_HVOR") }?.svar!!.first().verdi

        val oppholdUtenforNorgeNaarDato =
            oppholdUtenforNorgeSpoersmaalGruppering.undersporsmal
                .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_NAAR") }

        OppholdUtenforNorge(
            id = it.id,
            land = oppholdUtenforNorgeHvorVerdi,
            grunn = oppholdUtenforNorgeBegrunnelse?.sporsmalstekst ?: "null",
            perioder = mapBrukerSpoersmaalNaarDato(oppholdUtenforNorgeNaarDato.svar)
        )
    } ?: emptyList()
}
