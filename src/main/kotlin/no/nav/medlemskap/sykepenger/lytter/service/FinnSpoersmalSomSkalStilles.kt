package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import org.slf4j.MarkerFactory

private val log = KotlinLogging.logger { }
private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

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

fun opprettResponsTilFlex(
    foreløpigResponse: FlexRespons,
    forrigeBrukerspørsmål: List<Spørsmål>,
    fnr: String): FlexRespons {

    val spørsmålSomSkalStilles = finnSpørsmålSomSkalStilles(
        foreløpigResponse.sporsmal,
        forrigeBrukerspørsmål.toSet()
    )

    log.info(
        teamLogs,
        "Sammenstiller brukerspørsmål for: $fnr",
        kv("Forrige brukerspørsmål (innenfor levetid)", forrigeBrukerspørsmål),
        kv("Foreslåtte brukerspørsmål", foreløpigResponse.sporsmal),
        kv("Spørsmål som skal stilles", spørsmålSomSkalStilles)
    )

    return FlexRespons(
        svar = foreløpigResponse.svar,
        sporsmal = spørsmålSomSkalStilles
    )
}