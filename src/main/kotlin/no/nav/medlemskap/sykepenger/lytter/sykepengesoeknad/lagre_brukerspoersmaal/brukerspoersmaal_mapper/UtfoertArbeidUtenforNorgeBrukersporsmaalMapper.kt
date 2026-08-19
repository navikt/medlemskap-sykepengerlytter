package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapUtførtArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapSvar

fun hentUtførtArbeidUtenforNorgeBrukerSpørsmål(
    utførtArbeidUtenforNorgeSpørsmål: MedlemskapsBrukerSpørsmål?,
): MedlemskapUtførtArbeidUtenforNorge? {
    return if (utførtArbeidUtenforNorgeSpørsmål != null) {
        mapUtførtArbeidUtenforNorgeBrukerSpørsmål(utførtArbeidUtenforNorgeSpørsmål)
    } else {
        null
    }
}

private fun mapUtførtArbeidUtenforNorgeBrukerSpørsmål(
    utførtArbeidUtenforNorgeSpørsmål: MedlemskapsBrukerSpørsmål,
): MedlemskapUtførtArbeidUtenforNorge {
    val svar = mapSvar(utførtArbeidUtenforNorgeSpørsmål.svar)
    return MedlemskapUtførtArbeidUtenforNorge(
        id = utførtArbeidUtenforNorgeSpørsmål.id,
        spørsmålstekst = utførtArbeidUtenforNorgeSpørsmål.spørsmålstekst,
        svar = svar,
        arbeidUtenforNorge = if (svar) {
            mapUtførtArbeidUtenforNorgeUnderspørsmål(utførtArbeidUtenforNorgeSpørsmål.underspørsmål)
        } else {
            emptyList()
        },
    )
}

private fun mapUtførtArbeidUtenforNorgeUnderspørsmål(
    underspørsmål: List<MedlemskapsBrukerSpørsmål>?,
): List<ArbeidUtenforNorge> {
    return underspørsmål.orEmpty()
        .filter { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_GRUPPERING") }
        .map {
            val underspørsmål = it.underspørsmål.orEmpty()
            val utførtArbeidUtenforNorgeArbeidsgiverVerdi =
                underspørsmål.find { undersporsmal ->
                    undersporsmal.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_ARBEIDSGIVER")
                }?.svar?.first()?.verdi

            val utførtArbeidUtenforNorgeHvorVerdi =
                underspørsmål.first { undersporsmal ->
                    undersporsmal.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_HVOR")
                }.svar!!.first().verdi

            val utførtArbeidUtenforNorgeNårDato =
                underspørsmål.first { undersporsmal ->
                    undersporsmal.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_NAAR")
                }

        ArbeidUtenforNorge(
            id = it.id,
            arbeidsgiver = utførtArbeidUtenforNorgeArbeidsgiverVerdi ?: "null",
            land = utførtArbeidUtenforNorgeHvorVerdi,
            perioder = mapBrukerSpørsmålDato(utførtArbeidUtenforNorgeNårDato.svar),
        )
    }
}
