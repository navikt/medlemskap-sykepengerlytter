package no.nav.medlemskap.sykepenger.lytter.service

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