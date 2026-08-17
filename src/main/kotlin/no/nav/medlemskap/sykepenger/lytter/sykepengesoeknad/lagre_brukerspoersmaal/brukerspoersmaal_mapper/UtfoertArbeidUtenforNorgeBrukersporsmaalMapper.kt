package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.ArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.persistence.FlexMedlemskapsBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapUtfortArbeidUtenforNorge
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapBrukerSpoersmaalDato
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper.BrukerSpoersmaalMapperHjelper.mapSvar

fun getutfoertArbeidUtenforNorgeBrukerSporsmaal(
    utfoertArbeidUtenforNorgeSpoersmaal: FlexMedlemskapsBrukerSporsmaal?,
): MedlemskapUtfortArbeidUtenforNorge? {
    return if (utfoertArbeidUtenforNorgeSpoersmaal != null) {
        mapUtfoertArbeidUtenforNorge_BrukerSpoersmaal(utfoertArbeidUtenforNorgeSpoersmaal)
    } else {
        null
    }
}

private fun mapUtfoertArbeidUtenforNorge_BrukerSpoersmaal(
    utfoertArbeidUtenforNorgeSpoersmaal: FlexMedlemskapsBrukerSporsmaal,
): MedlemskapUtfortArbeidUtenforNorge {
    val svar = mapSvar(utfoertArbeidUtenforNorgeSpoersmaal.svar)
    return MedlemskapUtfortArbeidUtenforNorge(
        id = utfoertArbeidUtenforNorgeSpoersmaal.id,
        sporsmalstekst = utfoertArbeidUtenforNorgeSpoersmaal.sporsmalstekst,
        svar = svar,
        arbeidUtenforNorge = if (svar) {
            mapUtfoertArbeidUtenforNorgeUnderspoersmaal(utfoertArbeidUtenforNorgeSpoersmaal.undersporsmal)
        } else {
            emptyList()
        },
    )
}

private fun mapUtfoertArbeidUtenforNorgeUnderspoersmaal(
    underspoersmaal: List<FlexMedlemskapsBrukerSporsmaal>?,
): List<ArbeidUtenforNorge> {
    return underspoersmaal.orEmpty()
        .filter { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_GRUPPERING") }
        .map {
            val undersporsmal = it.undersporsmal.orEmpty()
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
            perioder = mapBrukerSpoersmaalDato(utfoertArbeidUtenforNorgeNaarDato.svar),
        )
    }
}
