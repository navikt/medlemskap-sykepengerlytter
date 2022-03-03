package no.nav.medlemskap.sykepenger.lytter.domain

import com.fasterxml.jackson.databind.JsonNode

data class SoknadRecord(val partition:Int,val offset:Long,val value : String, val key:String?,val topic:String,val sykepengeSoknad:LovmeSoknadDTO)
data class MedlemskapVurdertRecord(val partition:Int,val offset:Long,val value : String, val key:String?,val topic:String,val medlemskapVurdert:JsonNode)
