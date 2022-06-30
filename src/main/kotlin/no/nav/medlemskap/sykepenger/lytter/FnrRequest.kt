package no.nav.medlemskap.sykepenger.lytter

import java.util.*

data class FnrRequest(
    val fnr: String,
)
data class FnrResponse(
    val id: String,
    val soknadId: String,
    val date: Date,
    val status: String,
    val aarsaker: Collection<aarsaker>?
)
data class aarsaker (
    val regelId:String,
    val begrunnelse:String
    )


