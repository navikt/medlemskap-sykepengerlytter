package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål

fun finnSpørsmålSomSkalStilles(
    potensielle: Set<Spørsmål>,
    forrigeStilte: Set<Spørsmål>
): Set<Spørsmål> {

    if (forrigeStilte.isEmpty()) return potensielle

    return when {
        potensielle == forrigeStilte -> emptySet()
        potensielle.containsAll(forrigeStilte) && potensielle.size > forrigeStilte.size -> potensielle
        potensielle.all { it in forrigeStilte } -> emptySet()
        else -> potensielle
    }
}

fun opprettResponsTilFlex(foreløpigResponse: FlexRespons, forrigeBrukerspørsmål: List<Spørsmål>): FlexRespons {

    val spørsmålSomSkalStilles = finnSpørsmålSomSkalStilles(
        foreløpigResponse.sporsmal,
        forrigeBrukerspørsmål.toSet()
    )
    return FlexRespons(
        svar = foreløpigResponse.svar,
        sporsmal = spørsmålSomSkalStilles
    )

}


fun test(a: String) : String? {
    return a.takeIf { a == "a" }
}