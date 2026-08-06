package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag

object MedlemskapOppslagRequestMapper {

    fun tilMedlemskapOppslagRequest(
        sykepengeSøknad: SykepengesoeknadGrunnlag,
        brukerinput: Brukerinput
    ): MedlOppslagRequest =
        MedlOppslagRequest(
            fnr = sykepengeSøknad.fnr,
            førsteDagForYtelse = sykepengeSøknad.fom.toString(),
            periode = Periode(sykepengeSøknad.fom.toString(), sykepengeSøknad.tom.toString()),
            brukerinput = brukerinput
        )

}
