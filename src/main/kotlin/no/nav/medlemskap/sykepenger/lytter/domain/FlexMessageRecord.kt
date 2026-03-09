package no.nav.medlemskap.sykepenger.lytter.domain

import java.time.LocalDateTime


enum class Kilde {
    KAFKA,
    LOVME_GCP
}

data class FlexMessageRecord(val partition:Int, val offset:Long, val value : String, val key:String?, val topic:String, val timestamp: LocalDateTime, val timestampType:String, val kilde: Kilde = Kilde.KAFKA)

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