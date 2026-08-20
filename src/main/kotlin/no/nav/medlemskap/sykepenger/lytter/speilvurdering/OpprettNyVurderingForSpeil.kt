package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Speilvurdering
import no.nav.medlemskap.sykepenger.lytter.service.UtledBrukerinput
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.service.MedlemskapOppslagService

class OpprettNyVurderingForSpeil(
    private val medlemskapOppslagService: MedlemskapOppslagService,
    private val medlemskapOppslagMapper: MedlemskapOppslagMapper,
    private val utledBrukerinput: UtledBrukerinput,
    private val speilvurderingMapper: SpeilvurderingMapper = SpeilvurderingMapper()
) {
    suspend fun opprett(request: SpeilvurderingRequest, callId: String): Speilvurdering {
        val brukerinput = utledBrukerinput.fraSpeilRequest(request, callId)
        val medlemskapOppslagRequest = medlemskapOppslagMapper.tilMedlemskapOppslagRequest(request, brukerinput)
        val medlemskapOppslagVurdering =
            medlemskapOppslagService.vurderMedlemskapForSpeil(medlemskapOppslagRequest, callId)
        return speilvurderingMapper.fraMedlemskapOppslag(medlemskapOppslagVurdering, callId)
    }
}
