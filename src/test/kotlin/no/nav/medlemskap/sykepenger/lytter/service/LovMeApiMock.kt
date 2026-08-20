package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapOppslagVurdering
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser


class LovMeApiMock(
    private val filer: Map<String, String> = emptyMap()
) : MedlemskapOppslagAPI {

    var request: MedlemskapOppslagRequest? = null

    private fun hentFil(nøkkel: String): String {
        val filnavn = filer[nøkkel]
            ?: error("Ingen fil konfigurert for nøkkel '$nøkkel'")
        return this::class.java.classLoader.getResource(filnavn)
            .readText(Charsets.UTF_8)
    }

    override suspend fun vurderMedlemskap(
        medlOppslagRequest: MedlemskapOppslagRequest,
        callId: String
    ): String {
        request = medlOppslagRequest
        return hentFil("vurderMedlemskap")
    }

    override suspend fun vurderMedlemskapForSpeil(
        medlOppslagRequest: MedlemskapOppslagRequest,
        callId: String
    ): MedlemskapOppslagVurdering {
        request = medlOppslagRequest
        return JacksonParser().toDomainObject(hentFil("vurderMedlemskapForSpeil"))
    }

    override suspend fun brukerspørsmål(
        medlOppslagRequest: MedlemskapOppslagRequest,
        callId: String
    ): String {
        request = medlOppslagRequest
        return hentFil("brukerspørsmål")
    }
}