package no.nav.medlemskap.sykepenger.lytter.service

class GenererBrukerSporsmaal {

    val ENKELTREGLER = listOf(
        "REGEL_3",
        "REGEL_19_3_1",
        "REGEL_15",
        "REGEL_C",
        "REGEL_12",
        "REGEL_20",
        "REGEL_34",
        "REGEL_21",
        "REGEL_25",
        "REGEL_10",
        "REGEL_5",
        "REGEL_1_3_1",
        "REGEL_1_3_3",
        "REGEL_1_3_4",
        "REGEL_1_3_5"
    )
    val MULTIREGLER_FOR_REGEL11 = "REGEL_11"

    fun skalGenerereBrukerSpørsmål(årsaker: List<String>): Boolean {
        val regelbrudd = årsaker

        if (regelbrudd.isEmpty()) return false


        return regelbrudd.all { regelbrudd ->
            erKjentRegelbrudd(regelbrudd)
        }
    }

    private fun erKjentRegelbrudd(regelbrudd: String): Boolean {
        return regelbrudd.erEnAv(ENKELTREGLER) ||
                regelbrudd.starterMed(MULTIREGLER_FOR_REGEL11)
    }

}

fun String.erEnAv(regler: List<String>): Boolean {
    return this in regler
}

fun String.starterMed(prefix: String): Boolean {
    return this.startsWith(prefix)
}