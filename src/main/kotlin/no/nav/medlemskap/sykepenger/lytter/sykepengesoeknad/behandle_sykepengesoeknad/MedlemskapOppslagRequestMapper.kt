package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.MedlemskapOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.clients.medlemskap_oppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag

object MedlemskapOppslagRequestMapper {

    fun tilMedlemskapOppslagRequest(
        sykepengeSøknad: SykepengesoeknadGrunnlag,
        brukerinput: Brukerinput
    ): MedlemskapOppslagRequest =
        MedlemskapOppslagRequest(
            fnr = sykepengeSøknad.fnr,
            førsteDagForYtelse = sykepengeSøknad.fom.toString(),
            periode = Periode(sykepengeSøknad.fom.toString(), sykepengeSøknad.tom.toString()),
            brukerinput = brukerinput
        )

}
