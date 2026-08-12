package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import java.time.LocalDate

data class Medlemskapsstatus(
    val fnr: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val medlem: ErMedlem
)

enum class ErMedlem {
    JA,
    NEI,
    UAVKLART,
    PAFOLGENDE
}
