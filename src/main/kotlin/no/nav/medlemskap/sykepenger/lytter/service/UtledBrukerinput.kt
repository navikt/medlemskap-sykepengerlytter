package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingRequest
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Sykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag

class UtledBrukerinput(
    private val gjenbrukBrukersvar: GjenbrukBrukersvar
) {
    fun fraSykepengesøknad(sykepengesøknad: Sykepengesoeknad): Brukerinput {
        val søknadsParametere = sykepengesøknad.sykepengesøknadGrunnlag.tilSøknadsParametere()

        return gjenbrukBrukersvar.fraInnkommendeSøknad(
            søknadsParametere = søknadsParametere,
            brukersvarPåInnkommendeSøknad = sykepengesøknad.brukerspørsmål
        )
    }

    fun fraSpeilRequest(request: SpeilvurderingRequest, callId: String): Brukerinput =
        gjenbrukBrukersvar.fraTidligereSvar(
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

private fun SpeilvurderingRequest.tilSøknadsParametere(callId: String): SoeknadsParametere =
    SoeknadsParametere(
        callId = callId,
        fnr = fnr,
        førsteDagForYtelse = førsteDagForYtelse.toString()
    )
