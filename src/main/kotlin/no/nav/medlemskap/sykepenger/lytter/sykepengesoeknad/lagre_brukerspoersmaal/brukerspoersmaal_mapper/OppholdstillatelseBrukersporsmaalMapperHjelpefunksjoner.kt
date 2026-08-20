package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapOppholdstillatelseBrukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.Periode
import no.nav.medlemskap.sykepenger.lytter.persistence.firstMedTagPrefiks
import no.nav.medlemskap.sykepenger.lytter.persistence.førsteSvarVerdi
import java.time.LocalDate

internal fun hentVedtaksdatoFraUnderspørsmål(underspørsmål: List<MedlemskapsBrukerSpørsmål>?): String {
    return underspørsmål?.firstMedTagPrefiks("MEDLEMSKAP_OPPHOLDSTILLATELSE_VEDTAKSDATO")?.førsteSvarVerdi()
        ?: "null"
}

internal fun lagOppholdstillatelseBrukerspørsmål(
    oppholdstillatelseBrukerspørsmål: MedlemskapsBrukerSpørsmål,
    svar: Boolean,
    vedtaksdato: LocalDate,
    vedtaksTypePermanent: Boolean,
    perioder: List<Periode>
): MedlemskapOppholdstillatelseBrukerspørsmål {
    return MedlemskapOppholdstillatelseBrukerspørsmål(
        id = oppholdstillatelseBrukerspørsmål.id,
        sporsmalstekst = oppholdstillatelseBrukerspørsmål.sporsmalstekst,
        svar = svar,
        vedtaksdato = vedtaksdato,
        vedtaksTypePermanent = vedtaksTypePermanent,
        perioder = perioder
    )
}

internal class VedtaksType(val erPermanentVedtaksType: Boolean, val periode: List<Periode>)
