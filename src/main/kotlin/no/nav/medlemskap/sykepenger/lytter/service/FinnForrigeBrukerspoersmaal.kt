package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_eos
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_opphold_utenfor_norge
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_oppholdstilatelse_brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.Medlemskap_utfort_arbeid_utenfor_norge
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import java.time.LocalDate

fun arbeidUtlandSpørsmålFraForrigeSøknad(
    forrigeKjente: Brukersporsmaal?,
    førsteDagForYtelse: LocalDate
): Medlemskap_utfort_arbeid_utenfor_norge? {

    val forrigeKjenteSpørsmålForArbeidUtland = forrigeKjente?.utfort_arbeid_utenfor_norge ?: return null

    if (forrigeKjenteSpørsmålForArbeidUtland.svar) return null

    val dager = antallDagerMellomToDatoer(førsteDagForYtelse, forrigeKjente.eventDate)
    if (dager >= Levetid.STANDARD_LEVETID_32.dager) return null

    return forrigeKjenteSpørsmålForArbeidUtland
}


fun oppholdUtenforNorgeSpørsmålFraForrigeSøknad(
    brukerspørsmålFraForrigeSøknad: Brukersporsmaal?,
    førsteDagForYtelse: LocalDate
): Medlemskap_opphold_utenfor_norge? {

    val forrigeKjenteSpørsmålForOppholdUtenforNorge = brukerspørsmålFraForrigeSøknad?.oppholdUtenforNorge ?: return null

    if (forrigeKjenteSpørsmålForOppholdUtenforNorge.svar) return null

    val dager = antallDagerMellomToDatoer(førsteDagForYtelse, brukerspørsmålFraForrigeSøknad.eventDate)
    if (dager >= Levetid.STANDARD_LEVETID_32.dager) return null

    return forrigeKjenteSpørsmålForOppholdUtenforNorge
}

fun oppholdUtenforEØSSpørsmålFraForrigeSøknad(
    brukerspørsmålFraForrigeSøknad: Brukersporsmaal?,
    førsteDagForYtelse: LocalDate
): Medlemskap_opphold_utenfor_eos? {

    val forrigeKjenteSpørsmålForOppholdUtenforEØS = brukerspørsmålFraForrigeSøknad?.oppholdUtenforEOS ?: return null

    if (forrigeKjenteSpørsmålForOppholdUtenforEØS.svar) return null

    val dager = antallDagerMellomToDatoer(førsteDagForYtelse, brukerspørsmålFraForrigeSøknad.eventDate)
    if (dager >= Levetid.STANDARD_LEVETID_32.dager) return null

    return forrigeKjenteSpørsmålForOppholdUtenforEØS
}

fun oppholdstillatelseSpørsmålFraForrigeSøknad(
    brukerspørsmålFraForrigeSøknad: Brukersporsmaal?,
    førsteDagForYtelse: LocalDate
): Medlemskap_oppholdstilatelse_brukersporsmaal? {

    val forrigeKjenteSpørsmålForOppholdstillatelse = brukerspørsmålFraForrigeSøknad?.oppholdstilatelse ?: return null

    if (!forrigeKjenteSpørsmålForOppholdstillatelse.svar) return null

    val dager = antallDagerMellomToDatoer(førsteDagForYtelse, brukerspørsmålFraForrigeSøknad.eventDate)
    if (dager >= Levetid.STANDARD_LEVETID_32.dager) return null

    return forrigeKjenteSpørsmålForOppholdstillatelse
}

fun finnForrigeBrukersporsmaal(forrigeKjente: Brukersporsmaal?, førsteDagForYtelse: LocalDate): List<Spørsmål> {

    //Skal finne ut om vi stilte disse brukerspørsmålene forrige gang
    val sammenstilteSpørsmål = listOf(
        arbeidUtlandSpørsmålFraForrigeSøknad(
            forrigeKjente,
            førsteDagForYtelse
        ) to Spørsmål.ARBEID_UTENFOR_NORGE,

        oppholdUtenforNorgeSpørsmålFraForrigeSøknad(
            forrigeKjente,
            førsteDagForYtelse
        ) to Spørsmål.OPPHOLD_UTENFOR_NORGE,

        oppholdUtenforEØSSpørsmålFraForrigeSøknad(
            forrigeKjente,
            førsteDagForYtelse
        ) to Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE,

        oppholdstillatelseSpørsmålFraForrigeSøknad(
            forrigeKjente,
            førsteDagForYtelse
        ) to Spørsmål.OPPHOLDSTILATELSE
    )


    val alleredespurteBrukersporsmål = mutableListOf<Spørsmål>()

    //Resulterer i en liste med de brukerspørsmålene som faktisk ble stilt forrige gang
    sammenstilteSpørsmål.forEach { (result, spm) ->
        result?.let { alleredespurteBrukersporsmål.add(spm) }
    }

    return alleredespurteBrukersporsmål
}