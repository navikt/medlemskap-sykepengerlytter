package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Periode

    fun medlemskapOppslagRequest(variables: Map<String, String>): MedlemskapOppslagRequest{
        return MedlemskapOppslagRequest(
            fnr = variables["fnr"]!!,
            førsteDagForYtelse = variables["fom"]!!,
            periode = Periode(
                fom=variables["fom"]!!,
                tom = variables["tom"]!!),
            brukerinput = Brukerinput(false))
    }
