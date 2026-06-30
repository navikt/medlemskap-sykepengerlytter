package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag

object MedlemskapOppslagRequestMapper {
    fun map(
        sykepengeSoknad: SykepengesoeknadGrunnlag,
        utledetBrukerinput: UtledetBrukerinput
    ): MedlOppslagRequest {
        val søknadsParametere = utledetBrukerinput.søknadsParametere
        return MedlOppslagRequest(
            fnr = søknadsParametere.fnr,
            førsteDagForYtelse = søknadsParametere.førsteDagForYtelse,
            periode = Periode(sykepengeSoknad.fom.toString(), sykepengeSoknad.tom.toString()),
            brukerinput = utledetBrukerinput.brukerinput
        )
    }
}
