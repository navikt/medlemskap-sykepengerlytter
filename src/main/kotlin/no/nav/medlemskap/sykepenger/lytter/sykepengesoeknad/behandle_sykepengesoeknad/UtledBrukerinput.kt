package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.Sykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import no.nav.medlemskap.sykepenger.lytter.service.BrukersvarGjenbruk
import no.nav.medlemskap.sykepenger.lytter.service.Kilde
import no.nav.medlemskap.sykepenger.lytter.service.SoeknadsParametere

class UtledBrukerinput(
    private val brukersvarGjenbruk: BrukersvarGjenbruk
) {
    fun utledBrukerinput(sykepengesøknad: Sykepengesoeknad): UtledetBrukerinput {
        val sykepengesøknadGrunnlag = sykepengesøknad.sykepengesøknadGrunnlag
        val søknadsParametere = sykepengesøknadGrunnlag.tilSøknadsParametere()

        val brukerinput = brukersvarGjenbruk.vurderGjenbrukAvBrukersvar(
            søknadsParametere,
            sykepengesøknad.brukerspørsmål,
            Kilde.SYKEPENGEBACKEND
        )

        return UtledetBrukerinput(brukerinput)
    }
}

data class UtledetBrukerinput(val brukerinput: Brukerinput)

private fun SykepengesoeknadGrunnlag.tilSøknadsParametere(): SoeknadsParametere =
    SoeknadsParametere(
        callId = id,
        fnr = fnr,
        førsteDagForYtelse = fom.toString()
    )
