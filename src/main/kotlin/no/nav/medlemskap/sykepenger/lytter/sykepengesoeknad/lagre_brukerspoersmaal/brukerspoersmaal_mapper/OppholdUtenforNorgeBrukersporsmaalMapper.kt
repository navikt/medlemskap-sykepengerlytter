package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.OppholdUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapSvar

fun hentOppholdUtenforNorgeBrukerSpørsmål(
    oppholdUtenforNorgebrukerspørsmål: MedlemskapsBrukerSpørsmål?
): MedlemskapOppholdUtenforNorge? {
    return if (oppholdUtenforNorgebrukerspørsmål != null) {
        mapOppholdUtenforNorgeBrukerSpørsmål(oppholdUtenforNorgebrukerspørsmål)
    } else {
        null
    }
}

private fun mapOppholdUtenforNorgeBrukerSpørsmål(
    oppholdUtenforNorge: MedlemskapsBrukerSpørsmål,
): MedlemskapOppholdUtenforNorge {
    val svar = mapSvar(oppholdUtenforNorge.svar)
    return MedlemskapOppholdUtenforNorge(
        id = oppholdUtenforNorge.id,
        sporsmalstekst = oppholdUtenforNorge.spørsmålstekst,
        svar = svar,
        oppholdUtenforNorge = if (svar) mapOppholdUtenforNorgeUnderspørsmål(oppholdUtenforNorge.underspørsmål) else emptyList()
    )
}

private fun mapOppholdUtenforNorgeUnderspørsmål(
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): List<OppholdUtenforNorge> {
    return underspørsmål.orEmpty()
        .filter { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_GRUPPERING") }
        .map {
            val underspørsmål = it.underspørsmål.orEmpty()

        val oppholdUtenforNorgeBegrunnelseUnderspørsmål = underspørsmål
            .find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_BEGRUNNELSE") }?.underspørsmål

        val oppholdUtenforNorgeBegrunnelseSpørsmålstekst = oppholdUtenforNorgeBegrunnelseUnderspørsmål?.find {
            it.svar?.size == 1
        }?.spørsmålstekst

        val oppholdUtenforNorgeHvorVerdi = underspørsmål
            .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_HVOR") }.svar!!.first().verdi

        val oppholdUtenforNorgeNårDato =
            underspørsmål
                .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE_NAAR") }

        OppholdUtenforNorge(
            id = it.id,
            land = oppholdUtenforNorgeHvorVerdi,
            grunn = oppholdUtenforNorgeBegrunnelseSpørsmålstekst ?: "null",
            perioder = mapBrukerSpørsmålDato(oppholdUtenforNorgeNårDato.svar)
        )
    }
}
