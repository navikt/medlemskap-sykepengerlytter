package no.nav.medlemskap.sykepenger.lytter.domain

data class LovMeRequest(val fnr:String?, val førsteDagForYtelse:String?, val peroode:Periode?, val brukerinput:Brukerinput?)

data class Periode(val fom:String,val tom:String)
data class Brukerinput(val arbeidUtenforNorge:Boolean)


/**
 * {
"fnr":"24446711111",
"førsteDagForYtelse": "2021-01-01",
"periode": {
"fom": "2021-01-01",
"tom": "2021-01-07"
},
"brukerinput": {
"arbeidUtenforNorge": false
}
}
 */
