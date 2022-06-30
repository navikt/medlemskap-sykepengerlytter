package no.nav.medlemskap.saga.persistence


import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import java.time.LocalDate
import java.util.*

data class VurderingDao(val id: String, val fnr: String, val fom: LocalDate, val tom: LocalDate, val status: String)

data class SykepengerVurderingDao(val id: String, val soknadId: String, val date: Date, val json: String)
data class Periode(val fom: LocalDate, val tom: LocalDate)

fun Periode.begynnerIPerioden(periode: Periode): Boolean {
    return (
            (fom.isEqual(periode.fom)) ||
                    (fom.isAfter(periode.fom) && fom.isBefore(periode.tom))
            )
}


fun SykepengerVurderingDao.fnr(): String {
    return objectMapper.readTree(json).get("datagrunnlag").get("fnr").asText()
}

fun SykepengerVurderingDao.periode(): Periode {
    val periode = objectMapper.readTree(json).get("datagrunnlag").get("periode")
    return Periode(LocalDate.parse(periode.get("fom").asText()), LocalDate.parse(periode.get("tom").asText()));

}

data class Brukersporsmaal(
    val fnr: String,
    val soknadid: String,
    val eventDate: LocalDate,
    val ytelse: String,
    val status: String,
    val sporsmaal: FlexBrukerSporsmaal?
)

data class FlexBrukerSporsmaal(
    val arbeidUtland: Boolean?
)



