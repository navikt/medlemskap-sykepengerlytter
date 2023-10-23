package no.nav.medlemskap.sykepenger.lytter.clients.medloppslag
import java.time.LocalDate
import java.util.*


data class MedlOppslagRequest(
    val fnr: String,
    val førsteDagForYtelse:String,
    val periode: Periode,
    val brukerinput: Brukerinput
)

data class Periode(val fom: String, val tom: String= Date().toString())

data class Oppholdstilatelse(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val vedtaksdato: LocalDate,
    val vedtaksTypePermanent:Boolean,
    val perioder:List<Periode> = mutableListOf()
)
data class UtfortAarbeidUtenforNorge(
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

data class Brukerinput(
    val arbeidUtenforNorge: Boolean,
    val oppholdstilatelse:Oppholdstilatelse?=null,
    val utfortAarbeidUtenforNorge:UtfortAarbeidUtenforNorge?=null,
    val oppholdUtenforEos:OppholdUtenforEos?=null,
    val oppholdUtenforNorge:OppholdUtenforNorge?=null
)
data class OppholdUtenforNorge(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val oppholdUtenforNorge:List<Opphold>
)

data class OppholdUtenforEos(
    val id: String,
    val sporsmalstekst: String?,
    val svar:Boolean,
    val oppholdUtenforEOS:List<Opphold>
)

data class Opphold(
    val id: String,
    val land:String,
    val grunn:String,
    val perioder: List<Periode>
)
