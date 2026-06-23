package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import java.time.LocalDateTime


enum class Kilde {
    KAFKA,
    LOVME_GCP
}

data class SykepengesoeknadRecord(val partition:Int, val offset:Long, val value : String, val key:String?, val topic:String, val timestamp: LocalDateTime, val timestampType:String, val kilde: Kilde = Kilde.KAFKA)

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