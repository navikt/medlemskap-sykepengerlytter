package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering

import no.nav.medlemskap.sykepenger.lytter.domain.inneholderNyModellForBrukerspørsmål

class SpeilResponsMapper {

    companion object {
        private const val KAFKA_KANAL = "kafka"
        private const val SYKEPENGER = "SYKEPENGER"
    }

    fun mapTilSpeilRespons(medlemskapsVurdering: Medlemskapsvurdering): SpeilRespons? =
        medlemskapsVurdering
            .takeIf { it.kanal.equals(KAFKA_KANAL, ignoreCase = true) }
            ?.takeIf { it.ytelse == SYKEPENGER }
            ?.tilSpeilRespons()

    private fun Medlemskapsvurdering.tilSpeilRespons(): SpeilRespons =
        SpeilRespons(
            soknadId = vurderingsId,
            fnr = fnr,
            speilSvar = finnSpeilsvar(),
        )

    private fun Medlemskapsvurdering.finnSpeilsvar(): Speilsvar {
        val svargrunnlag = status.ifBlank { svar }
        if (svargrunnlag == Speilsvar.UAVKLART.name && brukerinput.inneholderNyModellForBrukerspørsmål()) {
            return Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL
        }
        return Speilsvar.valueOf(svargrunnlag)
    }
}
