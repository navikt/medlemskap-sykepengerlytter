package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdUtenforEØS
import no.nav.medlemskap.sykepenger.lytter.persistence.OppholdUtenforEØS
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapSvar

fun hentOppholdUtenforEØSBrukerSpørsmål(
    oppholdUtenforEØSbrukerspørsmål: MedlemskapsBrukerSpørsmål?
): MedlemskapOppholdUtenforEØS? {
    return if (oppholdUtenforEØSbrukerspørsmål != null) {
        mapOppholdUtenforEØSbrukerSpørsmål(oppholdUtenforEØSbrukerspørsmål)
    } else {
        null
    }
}

private fun mapOppholdUtenforEØSbrukerSpørsmål(
    oppholdutenforEØS: MedlemskapsBrukerSpørsmål
): MedlemskapOppholdUtenforEØS {
    val svar = mapSvar(oppholdutenforEØS.svar)
    return MedlemskapOppholdUtenforEØS(
        id = oppholdutenforEØS.id,
        sporsmalstekst = oppholdutenforEØS.sporsmalstekst,
        svar = svar,
        oppholdUtenforEOS = if (svar) mapOppholdUtenforEØSunderspørsmål(oppholdutenforEØS.undersporsmal) else emptyList(),
    )
}

private fun mapOppholdUtenforEØSunderspørsmål(
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?
): List<OppholdUtenforEØS> {
    return underspørsmål.orEmpty()
        .filter { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_GRUPPERING") }
        .map {
            val underspørsmål = it.undersporsmal.orEmpty()

            val oppholdUtenforEØSbegrunnelseSpørsmål = underspørsmål
                .find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_BEGRUNNELSE") }?.undersporsmal

            val oppholdUtenforEØSbegrunnelseSpørsmålstekst =
                oppholdUtenforEØSbegrunnelseSpørsmål?.find { it.svar?.size == 1 }?.sporsmalstekst

            val oppholdUtenforEØSspørsmålHvorVerdi = underspørsmål
                .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_HVOR") }.svar!!.first().verdi

            val oppholdUtenforEØSNårDato =
                underspørsmål
                    .first { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_NAAR") }

            OppholdUtenforEØS(
                id = it.id,
                land = oppholdUtenforEØSspørsmålHvorVerdi,
                grunn = oppholdUtenforEØSbegrunnelseSpørsmålstekst ?: "null",
                perioder = mapBrukerSpørsmålDato(oppholdUtenforEØSNårDato.svar),
            )
        }
}