package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain

enum class Status {
    NY,
    SENDT,
    FREMTIDIG,
    UTKAST_TIL_KORRIGERING,
    KORRIGERT,
    AVBRUTT,
    UTGATT,
    SLETTET
}