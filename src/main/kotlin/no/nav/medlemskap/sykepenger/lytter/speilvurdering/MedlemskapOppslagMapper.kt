package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.service.UtledBrukerinput

class MedlemskapOppslagMapper(
    private val utledBrukerinput: UtledBrukerinput
) {
    fun map(callId: String, request: SpeilvurderingRequest): MedlOppslagRequest {
        val brukerinput = utledBrukerinput.fraSpeilRequest(request, callId)

        return MedlOppslagRequest(
            fnr = request.fnr,
            førsteDagForYtelse = request.førsteDagForYtelse.toString(),
            periode = Periode(request.periode.fom.toString(), request.periode.tom.toString()),
            brukerinput = brukerinput
        )
    }
}
