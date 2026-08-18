package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlemskapOppslagAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapOppslagVurdering
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser


class LovMeApiMock(
    private val filer: Map<String, String> = emptyMap()
) : MedlemskapOppslagAPI {

    var request: MedlOppslagRequest? = null

    private fun hentFil(nøkkel: String): String {
        val filnavn = filer[nøkkel]
            ?: error("Ingen fil konfigurert for nøkkel '$nøkkel'")
        return this::class.java.classLoader.getResource(filnavn)
            .readText(Charsets.UTF_8)
    }

    override suspend fun vurderMedlemskap(
        medlOppslagRequest: MedlOppslagRequest,
        callId: String
    ): String {
        request = medlOppslagRequest
        return hentFil("vurderMedlemskap")
    }

    override suspend fun vurderMedlemskapForSpeil(
        medlOppslagRequest: MedlOppslagRequest,
        callId: String
    ): MedlemskapOppslagVurdering {
        request = medlOppslagRequest
        return JacksonParser().toDomainObject(hentFil("vurderMedlemskapBomlo"))
    }

    override suspend fun brukerspørsmål(
        medlOppslagRequest: MedlOppslagRequest,
        callId: String
    ): String {
        request = medlOppslagRequest
        return hentFil("brukerspørsmål")
    }
}