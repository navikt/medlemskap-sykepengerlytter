package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.aarsaker

class GenererBrukerSporsmaal {

    val ENKELTREGLER = listOf("REGEL_3","REGEL_19_3_1", "REGEL_15","REGEL_C","REGEL_12", "REGEL_20", "REGEL_34", "REGEL_21", "REGEL_25", "REGEL_10", "REGEL_5")
    val MULTIREGLER_FOR_REGEL11 = listOf("REGEL_11")
    val MEDL_REGLER = listOf("REGEL_1_3_1","REGEL_1_3_3","REGEL_1_3_4","REGEL_1_3_5")

    fun skalGenerereBrukerSpørsmål(årsaker: List<String>): Boolean {
        if (årsaker.isEmpty()) {
            return false
        }
        return when (årsaker.size) {

            1 -> {
                val regelbrudd = årsaker.first()
                regelbrudd.erEnAv(ENKELTREGLER) ||
                        regelbrudd.erEnAv(MEDL_REGLER) ||
                        MULTIREGLER_FOR_REGEL11.any { regelbrudd.startsWith(it) }
            } 
            else -> {
                val flereRegelbrudd = årsaker
                flereRegelbrudd.all { regelbrudd ->
                    regelbrudd.erEnAv(ENKELTREGLER) ||
                            regelbrudd.erEnAv(MEDL_REGLER) ||
                            MULTIREGLER_FOR_REGEL11.any { regelbrudd.startsWith(it) }

                }
            }
        }
    }
    fun String.erEnAv(regler: List<String>): Boolean {
        return this in regler
    }
}