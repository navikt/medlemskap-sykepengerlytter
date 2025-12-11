package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål

fun filtrerSpørsmålSomSkalStilles(
    potensielle: Set<Spørsmål>,
    forrigeStilte: Set<Spørsmål>
): Set<Spørsmål> {

    // Hvis ingen tidligere spørsmål, still alle potensielle
    if (forrigeStilte.isEmpty()) return potensielle

    return when {
        // Identiske sett -> ikke stille noe
        potensielle == forrigeStilte -> emptySet()

        // Potensielle er superset av forrige (inkluderer alt + minst ett nytt) -> still alle potensielle
        potensielle.containsAll(forrigeStilte) && potensielle.size > forrigeStilte.size -> potensielle

        // Potensielle er subset av forrige -> ikke stille noe
        potensielle.all { it in forrigeStilte } -> emptySet()

        // Delvis overlapp eller ingen overlapp ->  still alle potensielle
        else -> potensielle
    }
}