package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal

data class NormalisertBrukersvar(
    val arbeidUtenforNorge: Boolean?,
    val oppholdUtenforNorge: Boolean?,
    val oppholdUtenforEos: Boolean?,
    val oppholdstillatelse: Boolean?
)


fun Brukersporsmaal.normaliser(): NormalisertBrukersvar =
    NormalisertBrukersvar(
        arbeidUtenforNorge = this.utfort_arbeid_utenfor_norge?.svar,
        oppholdUtenforNorge = this.oppholdUtenforNorge?.svar,
        oppholdUtenforEos = this.oppholdUtenforEOS?.svar,
        oppholdstillatelse = this.oppholdstilatelse?.svar
    )

fun NormalisertBrukersvar.erGjenbrukbart(): Boolean {
    return when {
        arbeidUtenforNorge == false && oppholdUtenforNorge == false && oppholdstillatelse == true -> true

        arbeidUtenforNorge == false && oppholdUtenforNorge == false && oppholdstillatelse == null -> true

        arbeidUtenforNorge == false && oppholdUtenforEos == false && oppholdstillatelse == true -> true

        arbeidUtenforNorge == false && oppholdUtenforEos == false && oppholdstillatelse == null -> true

        else -> false
    }
}