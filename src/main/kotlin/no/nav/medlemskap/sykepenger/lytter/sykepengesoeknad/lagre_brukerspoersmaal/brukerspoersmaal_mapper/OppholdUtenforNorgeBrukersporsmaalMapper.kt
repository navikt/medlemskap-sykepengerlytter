package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.OppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapSvar

fun getOppholdUtenforNorgeBrukerSporsmaal(
    oppholdUtenforNorgebrukerspoersmaal: MedlemskapsBrukerSpørsmål?
): MedlemskapOppholdUtenforNorge? {
    return if (oppholdUtenforNorgebrukerspoersmaal != null) {
        mapOppholdUtenforNorge_BrukerSporsmaal(oppholdUtenforNorgebrukerspoersmaal)
    } else {
        null
    }
}

private fun mapOppholdUtenforNorge_BrukerSporsmaal(
    oppholdUtenforNorge: MedlemskapsBrukerSpørsmål,
): MedlemskapOppholdUtenforNorge {
    val svar = mapSvar(oppholdUtenforNorge.svar)
    return MedlemskapOppholdUtenforNorge(
        id = oppholdUtenforNorge.id,
        sporsmalstekst = oppholdUtenforNorge.spørsmålstekst,
        svar = svar,
        oppholdUtenforNorge = if (svar) mapOppholdUtenforNorgeUnderspoersmaal(oppholdUtenforNorge.underspørsmål) else emptyList()
    )
}

private fun mapOppholdUtenforNorgeUnderspoersmaal(
    underspoersmaal: List<MedlemskapsBrukerSpørsmål>?
): List<OppholdUtenforNorge> {
    return underspoersmaal.orEmpty()
        .filter { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_GRUPPERING") }
        .map {
            val undersporsmal = it.underspørsmål.orEmpty()

        val oppholdUtenforNorgeBegrunnelseUndersporsmaal = undersporsmal
            .find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_BEGRUNNELSE") }?.underspørsmål

        val oppholdUtenforNorgeBegrunnelseSporsmaalstekst = oppholdUtenforNorgeBegrunnelseUndersporsmaal?.find {
            it.svar?.size == 1
        }?.spørsmålstekst

        val oppholdUtenforNorgeHvorVerdi = undersporsmal
            .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_HVOR") }.svar!!.first().verdi

        val oppholdUtenforNorgeNaarDato =
            undersporsmal
                .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_NAAR") }

        OppholdUtenforNorge(
            id = it.id,
            land = oppholdUtenforNorgeHvorVerdi,
            grunn = oppholdUtenforNorgeBegrunnelseSporsmaalstekst ?: "null",
            perioder = mapBrukerSpørsmålDato(oppholdUtenforNorgeNaarDato.svar)
        )
    }
}
