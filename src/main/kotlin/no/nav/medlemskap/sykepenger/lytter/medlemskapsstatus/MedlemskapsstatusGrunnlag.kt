package no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus

import no.nav.medlemskap.sykepenger.lytter.domain.Klassifisering
import no.nav.medlemskap.sykepenger.lytter.domain.Status
import java.time.LocalDate

fun List<Klassifisering>.finnGrunnlagForFørstegangssøknaden(påfølgende: Klassifisering): Klassifisering? =
    filter { it.tom < påfølgende.tom && it.status != Status.PAFOLGENDE }
        .maxByOrNull { it.tom }

fun List<Klassifisering>.finnKlassifiseringForPeriode(
    fom: LocalDate, tom: LocalDate
): Klassifisering? =
    firstOrNull {
        it.fom.isEqual(fom) &&
            it.tom.isEqual(tom)
    }
