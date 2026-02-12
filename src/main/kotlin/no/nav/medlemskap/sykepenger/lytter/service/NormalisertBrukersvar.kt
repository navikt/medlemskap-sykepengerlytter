package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal

data class NormalisertBrukersvar(
    val a: Boolean?, // utfort_arbeid_utenfor_norge
    val b: Boolean?, // oppholdUtenforNorge
    val c: Boolean?, // oppholdUtenforEOS
    val d: Boolean? // oppholdstilatelse
)


fun Brukersporsmaal.normaliser(): NormalisertBrukersvar =
    NormalisertBrukersvar(
        a = this.utfort_arbeid_utenfor_norge?.svar,
        b = this.oppholdUtenforNorge?.svar,
        c = this.oppholdUtenforEOS?.svar,
        d = this.oppholdstilatelse?.svar
    )

fun NormalisertBrukersvar.erGjenbrukbart(): Boolean {
    return when {
        // 1) A=nei, B=nei
        a == false && b == false -> true

        // 2) A=nei, C=nei
        a == false && c == false && d == null -> true

        // 3) A=nei, C=nei, D=ja
        a == false && c == false && d == true -> true

        // 4) A=nei, B=nei, D=ja
        a == false && b == false && d == true -> true

        else -> false
    }
}