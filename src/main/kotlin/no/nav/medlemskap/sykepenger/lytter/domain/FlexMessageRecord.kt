package no.nav.medlemskap.sykepenger.lytter.domain

import java.time.LocalDateTime


data class FlexMessageRecord(val partition:Int, val offset:Long, val value : String, val key:String?, val topic:String, val timestamp: LocalDateTime, val timestampType:String)

enum class Soknadstatus {
    NY,
    SENDT,
    FREMTIDIG,
    UTKAST_TIL_KORRIGERING,
    KORRIGERT,
    AVBRUTT,
    UTGATT,
    SLETTET
}