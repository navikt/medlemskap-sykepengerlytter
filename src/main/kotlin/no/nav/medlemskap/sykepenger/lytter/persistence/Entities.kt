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

fun List<MedlemskapsBrukerSpørsmål>.firstMedTagPrefiks(prefiks: String) =
    firstOrNull { it.tag.startsWith(prefiks) }

fun List<MedlemskapsBrukerSpørsmål>.filterMedTagPrefiks(prefiks: String) =
    filter { it.tag.startsWith(prefiks) }

fun MedlemskapsBrukerSpørsmål.førsteSvarVerdi(): String =
    svar.orEmpty().first().verdi

data class Brukerspørsmål(
    val fnr: String,
    val soknadid: String,
    val eventDate: LocalDate,
    val ytelse: String,
    val status: String,
    val sporsmaal: ArbeidUtenforNorgeSpørsmål?, //fases ut til fordel for nye spørsmål
    val oppholdstilatelse:MedlemskapOppholdstillatelseBrukerspørsmål? = null,
    val utfort_arbeid_utenfor_norge:MedlemskapUtførtArbeidUtenforNorge? = null,
    val oppholdUtenforNorge:MedlemskapOppholdUtenforNorge? = null,
    val oppholdUtenforEOS:MedlemskapOppholdUtenforEØS? = null
)

data class ArbeidUtenforNorgeSpørsmål(
    val arbeidUtland: Boolean?
)

data class MedlemskapsBrukerSpørsmål(
    val id: String,
    val tag: String,
    val sporsmalstekst: String?,
    val undertekst: String?,
    val svartype: String?,
    val kriterieForVisningAvUndersporsmal:String?,
    val svar:List<spørsmålSvar>?,
    val undersporsmal:List<MedlemskapsBrukerSpørsmål>?
)

data class spørsmålSvar(val verdi:String)

data class MedlemskapOppholdstillatelseBrukerspørsmål(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val vedtaksdato:LocalDate,
    val vedtaksTypePermanent:Boolean,
    val perioder:List<Periode> = mutableListOf()
)

data class MedlemskapUtførtArbeidUtenforNorge(
    val id: String,
    val spørsmålstekst: String?,
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

data class OppholdUtenforEØS(
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

data class MedlemskapOppholdUtenforEØS(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val oppholdUtenforEOS:List<OppholdUtenforEØS>
)
