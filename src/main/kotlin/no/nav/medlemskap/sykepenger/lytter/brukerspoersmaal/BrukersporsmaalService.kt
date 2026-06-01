package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.service.FinnForrigeBrukersvar
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

class BrukersporsmaalService(
    var persistenceService: PersistenceService = PersistenceService(
        medlemskapVurdertRepository = PostgresMedlemskapVurdertRepository(
            DataSourceBuilder(System.getenv()).getDataSource()
        ),
        brukersporsmaalRepository = PostgresBrukersporsmaalRepository(
            DataSourceBuilder(System.getenv()).getDataSource()
        )
    )
) {
    private val finnForrigeBrukersvar = FinnForrigeBrukersvar(persistenceService)

    fun finnForrigeBrukerspørsmål(medlemskapOppslagRequest: MedlOppslagRequest): List<Spørsmål> {
        return finnForrigeBrukersvar.finnForrigeStilteBrukerspørsmål(medlemskapOppslagRequest.fnr, medlemskapOppslagRequest.førsteDagForYtelse)
    }
}