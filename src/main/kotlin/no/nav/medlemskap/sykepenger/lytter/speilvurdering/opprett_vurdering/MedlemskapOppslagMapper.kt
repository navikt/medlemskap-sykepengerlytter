package no.nav.medlemskap.sykepenger.lytter.speilvurdering.opprett_vurdering

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest

object MedlemskapOppslagMapper {
    fun tilMedlemskapOppslagRequest(
        request: SpeilvurderingRequest,
        brukerinput: Brukerinput
    ): MedlOppslagRequest =
        MedlOppslagRequest(
            fnr = request.fnr,
            førsteDagForYtelse = request.førsteDagForYtelse.toString(),
            periode = Periode(request.periode.fom.toString(), request.periode.tom.toString()),
            brukerinput = brukerinput
        )
}