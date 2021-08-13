package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.domain.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.domain.Periode
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.domain.SykepengeSoknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LovMeService(
    private val configuration: Configuration,
)
{
    companion object {
        val log: Logger = LoggerFactory.getLogger(LovMeService::class.java)
    }
    val azureAdClient = AzureAdClient(configuration)
    val restClients = RestClients(
        azureAdClient = azureAdClient,
        configuration = configuration
    )
    val medlOppslagClient: MedlOppslagClient


    init {
    medlOppslagClient=restClients.medlOppslag(configuration.register.medlemskapOppslagBaseUrl)
    }

    suspend fun callLovMe(sykepengeSoknad: SykepengeSoknad)
    {
        println("Calling lovme with hardcoded vaules! dev only")
        val fnr = "21507300739"
        val forsteDagForYtelse="2021-01-01"
        val periode = Periode("2021-01-01","2021-01-07")
        val brukerinput = Brukerinput(false)
        //val callId = sykepengeSoknad.sykmeldingId
        medlOppslagClient.vurderMedlemskap(MedlOppslagRequest(fnr,forsteDagForYtelse, periode, brukerinput),"12345")
    }
    suspend fun handle(soknadRecord: SoknadRecord)
    {
        soknadRecord.log()
        callLovMe(soknadRecord.sykepengeSoknad)
    }
    private fun SoknadRecord.log() =
        LovMeService.log.info(
            "Received soknad  - id: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic"
        )
}
