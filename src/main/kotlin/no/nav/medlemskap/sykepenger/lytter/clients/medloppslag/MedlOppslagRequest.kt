package no.nav.medlemskap.sykepenger.lytter.clients.medloppslag


data class MedlOppslagRequest(
    val fnr: String,
    val førsteDagForYtelse:String,
    val periode: Periode,
    val brukerinput: Brukerinput
)

data class Periode(val fom: String, val tom: String)

data class Brukerinput(val arbeidUtenforNorge: Boolean)

/*
*
* {
 "fnr":"21507300739",
 "førsteDagForYtelse": "2021-01-01",
 "periode": {
    "fom": "2021-01-01",
    "tom": "2021-01-07"
 },
 "brukerinput": {
    "arbeidUtenforNorge": false
 }
}
*
* */