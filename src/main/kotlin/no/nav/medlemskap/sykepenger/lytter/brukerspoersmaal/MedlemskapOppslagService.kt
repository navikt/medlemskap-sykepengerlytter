package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.service.FinnForrigeBrukersvar
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import org.slf4j.MarkerFactory

class MedlemskapOppslagService(private val configuration: Configuration,
                               var persistenceService: PersistenceService=PersistenceService(
                                   medlemskapVurdertRepository = PostgresMedlemskapVurdertRepository(
                                       DataSourceBuilder(System.getenv()).getDataSource()
                                   ) ,
                                   brukersporsmaalRepository = PostgresBrukersporsmaalRepository(
                                       DataSourceBuilder(System.getenv()).getDataSource()
                                   )
                               )
) {

    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")
    }

    var medlemskapOppslagClient: LovmeAPI

    init {
        val azureAdClient = AzureAdClient(configuration)
        val restClients = RestClients(
            azureAdClient = azureAdClient,
            configuration = configuration
        )

        medlemskapOppslagClient = restClients.medlOppslag(configuration.register.medlemskapOppslagBaseUrl)
    }

    private val finnForrigeBrukersvar = FinnForrigeBrukersvar(persistenceService)

    fun finnForrigeBrukerspørsmål(medlemskapOppslagRequest: MedlOppslagRequest): List<Spørsmål> {
        return finnForrigeBrukersvar.finnForrigeStilteBrukerspørsmål(medlemskapOppslagRequest.fnr, medlemskapOppslagRequest.førsteDagForYtelse)
    }


    suspend fun kallMedlemskapOppslag(request: MedlOppslagRequest, callId: String): String {
        runCatching { medlemskapOppslagClient.brukerspørsmål(request, callId) }
            .onFailure {
                if (it.message?.contains("GradertAdresseException") == true) {
                    return "GradertAdresse"
                } else {
                    throw Exception("Teknisk feil ved kall mot Lovme. Årsak : ${it.message}")
                }
            }
            .onSuccess { return it }
        return "" //umulig å komme hit?

    }
}