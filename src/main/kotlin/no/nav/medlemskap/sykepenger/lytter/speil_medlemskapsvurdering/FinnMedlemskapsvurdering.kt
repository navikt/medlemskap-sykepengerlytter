package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import no.nav.medlemskap.sykepenger.lytter.domain.Brukerinput

class FinnMedlemskapsvurdering {

    companion object {
        private const val KAFKA_KANAL = "kafka"
        private const val SYKEPENGER = "SYKEPENGER"
    }

    fun finn(medlemskapVurdering: MedlemskapVurdering): SpeilRespons? =
        medlemskapVurdering
            .takeIf { it.kanal == KAFKA_KANAL && it.ytelse == SYKEPENGER }
            ?.tilSpeilRespons()

    private fun MedlemskapVurdering.tilSpeilRespons(): SpeilRespons =
        SpeilRespons(
            soknadId = vurderingsId,
            fnr = fnr,
            speilSvar = finnSpeilsvar(),
        )

    private fun MedlemskapVurdering.finnSpeilsvar(): Speilsvar {
        val svargrunnlag = status.ifBlank { svar }
        if (svargrunnlag == Speilsvar.UAVKLART.name && inneholderNyModellForBrukerspørsmål(brukerinput)) {
            return Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL
        }
        return Speilsvar.valueOf(svargrunnlag)
    }

    fun inneholderNyModellForBrukerspørsmål(brukerinput: Brukerinput): Boolean =
        brukerinput.oppholdstilatelse != null ||
            brukerinput.utfortAarbeidUtenforNorge != null ||
            brukerinput.oppholdUtenforNorge != null ||
            brukerinput.oppholdUtenforEos != null
}
