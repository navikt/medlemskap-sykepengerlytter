package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.rest.BomloRequest
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Sykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag

class UtledBrukerinput(
    private val gjenbrukBrukersvar: GjenbrukBrukersvar
) {
    fun fraSykepengesøknad(sykepengesøknad: Sykepengesoeknad): Brukerinput {
        val søknadsParametere = sykepengesøknad.sykepengesøknadGrunnlag.tilSøknadsParametere()

        return gjenbrukBrukersvar.vurderBrukersvar(
            søknadsParametere,
            sykepengesøknad.brukerspørsmål,
            Kilde.SYKEPENGEBACKEND
        )
    }

    fun fraSpeilRequest(request: BomloRequest, callId: String): Brukerinput =
        gjenbrukBrukersvar.vurderBrukersvar(
            søknadsParametere = request.tilSøknadsParametere(callId),
            kilde = Kilde.SPEIL
        )
}

private fun SykepengesoeknadGrunnlag.tilSøknadsParametere(): SoeknadsParametere =
    SoeknadsParametere(
        callId = id,
        fnr = fnr,
        førsteDagForYtelse = fom.toString()
    )

private fun BomloRequest.tilSøknadsParametere(callId: String): SoeknadsParametere =
    SoeknadsParametere(
        callId = callId,
        fnr = fnr,
        førsteDagForYtelse = førsteDagForYtelse.toString()
    )
