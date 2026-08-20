package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapUtførtArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.erSvarPåBrukerspørsmålJa

fun hentUtførtArbeidUtenforNorgeBrukerSpørsmål(
    spørsmålListe: List<MedlemskapsBrukerSpørsmål>,
): MedlemskapUtførtArbeidUtenforNorge? {
    val utførtArbeidUtenforNorgeSpørsmål =
        spørsmålListe.find { it.tag == "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE" }

    return if (utførtArbeidUtenforNorgeSpørsmål != null) {
        mapUtførtArbeidUtenforNorgeBrukerSpørsmål(utførtArbeidUtenforNorgeSpørsmål)
    } else {
        null
    }
}

private fun mapUtførtArbeidUtenforNorgeBrukerSpørsmål(
    utførtArbeidUtenforNorgeSpørsmål: MedlemskapsBrukerSpørsmål,
): MedlemskapUtførtArbeidUtenforNorge {
    val erSvarPåBrukerspørsmålJa = erSvarPåBrukerspørsmålJa(utførtArbeidUtenforNorgeSpørsmål.svar)
    return MedlemskapUtførtArbeidUtenforNorge(
        id = utførtArbeidUtenforNorgeSpørsmål.id,
        spørsmålstekst = utførtArbeidUtenforNorgeSpørsmål.sporsmalstekst,
        svar = erSvarPåBrukerspørsmålJa,
        arbeidUtenforNorge = if (erSvarPåBrukerspørsmålJa) {
            mapUtførtArbeidUtenforNorgeUnderspørsmål(utførtArbeidUtenforNorgeSpørsmål.undersporsmal)
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
            val underspørsmål = it.undersporsmal.orEmpty()
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
