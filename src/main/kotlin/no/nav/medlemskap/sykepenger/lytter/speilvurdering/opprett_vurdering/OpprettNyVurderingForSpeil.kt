package no.nav.medlemskap.sykepenger.lytter.speilvurdering.opprett_vurdering

import no.nav.medlemskap.sykepenger.lytter.service.UtledBrukerinput
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.domain.Speilvurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingMapper

class OpprettNyVurderingForSpeil(
    private val medlemskapOppslagService: MedlemskapOppslagService,
    private val utledBrukerinput: UtledBrukerinput,
    private val speilvurderingMapper: SpeilvurderingMapper = SpeilvurderingMapper()
) {
    suspend fun opprett(request: SpeilvurderingRequest, callId: String): Speilvurdering {
        val brukerinput = utledBrukerinput.fraSpeilRequest(request, callId)
        val medlemskapOppslagRequest =
            MedlemskapOppslagMapper.tilMedlemskapOppslagRequest(request, brukerinput)
        val medlemskapOppslagVurdering =
            medlemskapOppslagService.vurderMedlemskapForSpeil(medlemskapOppslagRequest, callId)
        return speilvurderingMapper.fraMedlemskapOppslag(medlemskapOppslagVurdering, callId)
    }
}