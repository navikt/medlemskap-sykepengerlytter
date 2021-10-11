package no.nav.medlemskap.sykepenger.lytter.domain

data class SoknadRecord(val partition:Int,val offset:Long,val value : String, val key:String?,val topic:String,val sykepengeSoknad:LovmeSoknadDTO)
