package no.nav.medlemskap.sykepenger.lytter.speilvurdering.opprett_vurdering

import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagAPI
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapOppslagVurdering

class MedlemskapOppslagService(private val medlemskapOppslagApi: MedlemskapOppslagAPI) {

    suspend fun vurderMedlemskapForSpeil(request: MedlemskapOppslagRequest, callId: String): MedlemskapOppslagVurdering =
        medlemskapOppslagApi.vurderMedlemskapForSpeil(request, callId)
}