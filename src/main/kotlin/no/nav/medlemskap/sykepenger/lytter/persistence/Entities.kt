package no.nav.medlemskap.sykepenger.lytter.persistence


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

fun Periode.erAvsluttetPr(date:LocalDate): Boolean {
    return this.tom.isBefore(date)
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
    val sporsmaal: FlexBrukerSporsmaal?, //fases ut til fordel for nye spørsmål
    val oppholdstilatelse:MedlemskapOppholdstillatelseBrukerspørsmål? = null,
    val utfortArbeidUtenforNorge:MedlemskapUtfortArbeidUtenforNorge? = null,
    val oppholdUtenforNorge:MedlemskapOppholdUtenforNorge? = null,
    val oppholdUtenforEOS:MedlemskapOppholdUtenforEOS? = null

)

data class FlexBrukerSporsmaal(
    val arbeidUtland: Boolean?
)

data class MedlemskapsBrukerSpørsmål(
    val id: String,
    val tag: String,
    val spørsmålstekst: String?,
    val undertekst: String?,
    val svartype: String?,
    val kriterieForVisningAvUnderspørsmål:String?,
    val svar:List<spørsmålSvar>?,
    val underspørsmål:List<MedlemskapsBrukerSpørsmål>?
)

data class spørsmålSvar(val verdi:String)

data class MedlemskapOppholdstillatelseBrukerspørsmål(
    val id: String,
    val spørsmalstekst: String?,
    val svar:Boolean,
    val vedtaksdato:LocalDate,
    val vedtaksTypePermanent:Boolean,
    val perioder:List<Periode> = mutableListOf()
)

data class MedlemskapUtfortArbeidUtenforNorge(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val arbeidUtenforNorge:List<ArbeidUtenforNorge>
)

data class ArbeidUtenforNorge(
    val id: String,
    val arbeidsgiver:String,
    val land:String,
    val perioder: List<Periode>
)

data class OppholdUtenforNorge(
    val id: String,
    val land:String,
    val grunn:String,
    val perioder: List<Periode>
)

data class OppholdUtenforEOS(
    val id: String,
    val land:String,
    val grunn:String,
    val perioder: List<Periode>
)

data class MedlemskapOppholdUtenforNorge(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val oppholdUtenforNorge:List<OppholdUtenforNorge>
)

data class MedlemskapOppholdUtenforEOS(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val oppholdUtenforEOS:List<OppholdUtenforEOS>
)

