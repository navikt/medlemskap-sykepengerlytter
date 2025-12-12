package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest


class LovMeApiMock(
    private val filer: Map<String, String> = emptyMap()
) : LovmeAPI {

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

    override suspend fun vurderMedlemskapBomlo(
        medlOppslagRequest: MedlOppslagRequest,
        callId: String
    ): String {
        request = medlOppslagRequest
        return hentFil("vurderMedlemskapBomlo")
    }

    override suspend fun brukerspørsmål(
        medlOppslagRequest: MedlOppslagRequest,
        callId: String
    ): String {
        request = medlOppslagRequest
        return hentFil("brukerspørsmål")
    }
}