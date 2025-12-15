package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FinnSpoersmalSomSkalStillesTest {

    @Test
    fun `når forrige brukerspørsmål er tom skal alltid foreslått spørsmål brukes`() {
        val forrige_tom = emptySet<Spørsmål>()
        val foreslått = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)

        val faktisk = finnSpørsmålSomSkalStilles(foreslått,forrige_tom)
        Assertions.assertEquals(foreslått, faktisk)
    }

    @Test
    fun `når foreslått spørsmål er ett nytt spørsmålsett så skal disse brukes`() {
        val forrige = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val foreslått = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE, Spørsmål.OPPHOLDSTILATELSE)

        val faktisk = finnSpørsmålSomSkalStilles(foreslått,forrige)
        Assertions.assertEquals(foreslått, faktisk)
    }

    @Test
    fun `når foreslått brukerspørsmål overlapper delvis så skal alltid foreslått spørsmål brukes`() {
        val forrige = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val foreslått = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_NORGE)

        val faktisk = finnSpørsmålSomSkalStilles(foreslått, forrige)
        Assertions.assertEquals(foreslått, faktisk)
    }

    @Test
    fun `når foreslått brukerspørsmål er et subset av forrige spørsmål så skal vi ikke bruke nye spørsmål`() {
        val forrige = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE, Spørsmål.OPPHOLDSTILATELSE)
        val foreslått = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)

        val faktisk = finnSpørsmålSomSkalStilles(foreslått, forrige)
        Assertions.assertEquals(emptySet<Spørsmål>(), faktisk)
    }

    @Test
    fun `når foreslått brukerspørsmål er identiske av forrige brukerspørsmål så skal vi ikke bruke nye spørsmål`() {
        val forrige = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val foreslått = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)

        val faktisk = finnSpørsmålSomSkalStilles(foreslått, forrige)
        Assertions.assertEquals(emptySet<Spørsmål>(), faktisk)
    }

    @Test
    fun `når foreslått brukerspørsmål er tom så skal vi ikke bruke nye spørsmål`() {
        val forrige = setOf(Spørsmål.ARBEID_UTENFOR_NORGE, Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE)
        val foreslått = emptySet<Spørsmål>()

        val faktisk = finnSpørsmålSomSkalStilles(foreslått, forrige)
        Assertions.assertEquals(emptySet<Spørsmål>(), faktisk)
    }
}