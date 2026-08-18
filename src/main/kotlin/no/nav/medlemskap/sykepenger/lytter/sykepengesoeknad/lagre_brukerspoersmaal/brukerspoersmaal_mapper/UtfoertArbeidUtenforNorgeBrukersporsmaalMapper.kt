package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapUtfortArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapBrukerSpørsmålDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSporsmaalMapperHjelper.mapSvar

fun getutfoertArbeidUtenforNorgeBrukerSporsmaal(
    utfoertArbeidUtenforNorgeSpoersmaal: MedlemskapsBrukerSpørsmål?,
): MedlemskapUtfortArbeidUtenforNorge? {
    return if (utfoertArbeidUtenforNorgeSpoersmaal != null) {
        mapUtfoertArbeidUtenforNorge_BrukerSpoersmaal(utfoertArbeidUtenforNorgeSpoersmaal)
    } else {
        null
    }
}

private fun mapUtfoertArbeidUtenforNorge_BrukerSpoersmaal(
    utfoertArbeidUtenforNorgeSpoersmaal: MedlemskapsBrukerSpørsmål,
): MedlemskapUtfortArbeidUtenforNorge {
    val svar = mapSvar(utfoertArbeidUtenforNorgeSpoersmaal.svar)
    return MedlemskapUtfortArbeidUtenforNorge(
        id = utfoertArbeidUtenforNorgeSpoersmaal.id,
        sporsmalstekst = utfoertArbeidUtenforNorgeSpoersmaal.spørsmålstekst,
        svar = svar,
        arbeidUtenforNorge = if (svar) {
            mapUtfoertArbeidUtenforNorgeUnderspoersmaal(utfoertArbeidUtenforNorgeSpoersmaal.underspørsmål)
        } else {
            emptyList()
        },
    )
}

private fun mapUtfoertArbeidUtenforNorgeUnderspoersmaal(
    underspoersmaal: List<MedlemskapsBrukerSpørsmål>?,
): List<ArbeidUtenforNorge> {
    return underspoersmaal.orEmpty()
        .filter { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_GRUPPERING") }
        .map {
            val undersporsmal = it.underspørsmål.orEmpty()
            val utfoertArbeidUtenforNorgeArbeidsgiverVerdi =
                undersporsmal.find { undersporsmal ->
                    undersporsmal.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_ARBEIDSGIVER")
                }?.svar?.first()?.verdi

            val utfoertArbeidUtenforNorgeHvorVerdi =
                undersporsmal.first { undersporsmal ->
                    undersporsmal.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_HVOR")
                }.svar!!.first().verdi

            val utfoertArbeidUtenforNorgeNaarDato =
                undersporsmal.first { undersporsmal ->
                    undersporsmal.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_NAAR")
                }

        ArbeidUtenforNorge(
            id = it.id,
            arbeidsgiver = utfoertArbeidUtenforNorgeArbeidsgiverVerdi ?: "null",
            land = utfoertArbeidUtenforNorgeHvorVerdi,
            perioder = mapBrukerSpørsmålDato(utfoertArbeidUtenforNorgeNaarDato.svar),
        )
    }
}
