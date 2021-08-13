package no.nav.medlemskap.sykepenger.lytter.clients.medloppslag

import no.nav.medlemskap.sykepenger.lytter.domain.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.domain.Periode

data class MedlOppslagRequest(
    val fnr: String,
    val førsteDagForYtelse:String,
    val periode: Periode,
    val brukerinput: Brukerinput
)
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