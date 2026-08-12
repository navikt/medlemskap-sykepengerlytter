package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

fun Medlemskapsstatus.erpåfølgende(medlemskap: Medlemskapsstatus): Boolean =
    fom.isEqual(medlemskap.tom.plusDays(1)) || erIMidtenAv(medlemskap)

fun Medlemskapsstatus.erFunkskjoneltLik(medlemskap: Medlemskapsstatus): Boolean =
    fom.isEqual(medlemskap.fom) && tom.isEqual(medlemskap.tom)

private fun Medlemskapsstatus.erIMidtenAv(medlemskap: Medlemskapsstatus): Boolean =
    fom.isAfter(medlemskap.fom) && fom.isBefore(medlemskap.tom)
