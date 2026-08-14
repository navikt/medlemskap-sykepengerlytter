package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.OppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapBrukerSpoersmaalDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapSvar

fun getOppholdUtenforNorgeBrukerSporsmaal(
    oppholdUtenforNorgebrukerspoersmaal: FlexMedlemskapsBrukerSporsmaal?
): MedlemskapOppholdUtenforNorge? {
    return if (oppholdUtenforNorgebrukerspoersmaal != null) {
        mapOppholdUtenforNorge_BrukerSporsmaal(oppholdUtenforNorgebrukerspoersmaal)
    } else {
        null
    }
}

private fun mapOppholdUtenforNorge_BrukerSporsmaal(
    oppholdUtenforNorge: FlexMedlemskapsBrukerSporsmaal,
): MedlemskapOppholdUtenforNorge {
    val svar = mapSvar(oppholdUtenforNorge.svar)
    return MedlemskapOppholdUtenforNorge(
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
            perioder = mapBrukerSpoersmaalDato(oppholdUtenforNorgeNaarDato.svar)
        )
    } ?: emptyList()
}
