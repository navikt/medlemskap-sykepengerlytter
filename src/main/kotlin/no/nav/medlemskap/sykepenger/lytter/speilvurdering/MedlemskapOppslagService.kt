package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlemskapOppslagAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapOppslagVurdering

class MedlemskapOppslagService(private val medlemskapOppslagApi: MedlemskapOppslagAPI) {

    suspend fun vurderMedlemskapForSpeil(request: MedlOppslagRequest, callId: String): MedlemskapOppslagVurdering =
        medlemskapOppslagApi.vurderMedlemskapForSpeil(request, callId)
}
