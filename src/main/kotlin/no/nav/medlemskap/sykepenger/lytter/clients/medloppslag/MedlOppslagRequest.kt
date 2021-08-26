package no.nav.medlemskap.sykepenger.lytter.clients.medloppslag

import java.util.*


data class MedlOppslagRequest(
    val fnr: String,
    val førsteDagForYtelse:String,
    val periode: Periode,
    val brukerinput: Brukerinput
)

data class Periode(val fom: String, val tom: String= Date().toString())

data class Brukerinput(val arbeidUtenforNorge: Boolean)