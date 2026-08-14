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
    val utfoertArbeidUtenforNorgeSpoersmaalGruppering =
        underspoersmaal?.first { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_GRUPPERING") }

    val utfoertArbeidUtenforNorgeArbeidsgiverVerdi = utfoertArbeidUtenforNorgeSpoersmaalGruppering
        ?.undersporsmal
        ?.find { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_ARBEIDSGIVER") }?.svar?.first()?.verdi

    val utfoertArbeidUtenforNorgeHvorVerdi = utfoertArbeidUtenforNorgeSpoersmaalGruppering
        ?.undersporsmal
        ?.find { it.tag.startsWith("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS_HVOR") }?.svar!!.first().verdi

    val utfoertArbeidUtenforNorgeNaarDato =
        utfoertArbeidUtenforNorgeSpoersmaalGruppering.undersporsmal
            .first { it.tag.startsWith("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_NAAR") }

    return underspoersmaal.map {
        ArbeidUtenforNorge(
            id = it.id,
            arbeidsgiver = utfoertArbeidUtenforNorgeArbeidsgiverVerdi ?: "null",
            land = utfoertArbeidUtenforNorgeHvorVerdi,
            perioder = mapBrukerSpoersmaalDato(utfoertArbeidUtenforNorgeNaarDato.svar),
        )
    } ?: emptyList()
}
