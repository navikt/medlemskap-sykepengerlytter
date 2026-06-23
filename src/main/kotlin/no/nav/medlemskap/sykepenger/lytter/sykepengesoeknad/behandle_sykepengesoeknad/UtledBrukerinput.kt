package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.service.BrukersvarGjenbruk
import no.nav.medlemskap.sykepenger.lytter.service.FinnForrigeBrukersvar
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.service.SoeknadsParametere

class UtledBrukerinput(
    private val persistenceService: PersistenceService
) {
    private val brukersvarGjenbruk = BrukersvarGjenbruk(FinnForrigeBrukersvar(persistenceService))

    fun utledBrukerinput(sykepengeSoknad: LovmeSoknadDTO): UtledetBrukerinput {
        val søknadsParametere = sykepengeSoknad.tilSøknadsParametere()

        val brukersvarPåInnkommendeSøknad =
            persistenceService.hentbrukersporsmaalForSoknadID(søknadsParametere.callId)

        val brukerinput = brukersvarGjenbruk.vurderGjenbrukAvBrukersvar(
            søknadsParametere,
            brukersvarPåInnkommendeSøknad,
            "sykepengebackend"
        )

        return UtledetBrukerinput(søknadsParametere, brukerinput)
    }
}

data class UtledetBrukerinput(
    val søknadsParametere: SoeknadsParametere,
    val brukerinput: Brukerinput
)

private fun LovmeSoknadDTO.tilSøknadsParametere(): SoeknadsParametere =
    SoeknadsParametere(
        callId = id,
        fnr = fnr,
        førsteDagForYtelse = fom.toString()
    )
