package no.nav.medlemskap.sykepenger.lytter.domain

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime

data class SoknadRecord(val partition:Int,val offset:Long,val value : String, val key:String?,val topic:String,val sykepengeSoknad:LovmeSoknadDTO)
data class SoknadRecordReplay(val partition:Int,val offset:Long,val value : String, val key:String?,val topic:String,val sykepengeSoknad:LovmeSoknadDTO,val timestamp: LocalDateTime,val timestampType:String)

data class MedlemskapVurdertRecord(val partition:Int,val offset:Long,val value : String, val key:String?,val topic:String,val medlemskapVurdert:JsonNode,val timestamp: LocalDateTime,val timestampType:String)
