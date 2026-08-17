package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.gjenbruk

import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.Spørsmål

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

fun Set<Spørsmål>.finnSpørsmålSomSkalStilles(
    gjenbrukbareSpørsmål: Collection<Spørsmål>
): Set<Spørsmål> =
    finnSpørsmålSomSkalStilles(
        potensielle = this,
        forrigeStilte = gjenbrukbareSpørsmål.toSet()
    )