package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode

    fun medlemskapOppslagRequest(variables: Map<String, String>): MedlOppslagRequest{
        return MedlOppslagRequest(
            fnr = variables["fnr"]!!,
            førsteDagForYtelse = variables["fom"]!!,
            periode = Periode(
                fom=variables["fom"]!!,
                tom = variables["tom"]!!),
            brukerinput = Brukerinput(false))
    }
