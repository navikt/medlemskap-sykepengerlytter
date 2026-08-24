package no.nav.medlemskap.sykepenger.lytter.speilvurdering.opprett_vurdering

import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest

object MedlemskapOppslagMapper {
    fun tilMedlemskapOppslagRequest(
        request: SpeilvurderingRequest,
        brukerinput: Brukerinput
    ): MedlemskapOppslagRequest =
        MedlemskapOppslagRequest(
            fnr = request.fnr,
            førsteDagForYtelse = request.førsteDagForYtelse.toString(),
            periode = Periode(request.periode.fom.toString(), request.periode.tom.toString()),
            brukerinput = brukerinput
        )
}