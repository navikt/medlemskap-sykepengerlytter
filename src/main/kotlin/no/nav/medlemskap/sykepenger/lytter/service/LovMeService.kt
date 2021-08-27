package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LovMeService(
    private val configuration: Configuration,
)
{
    companion object {
        private val log = KotlinLogging.logger { }

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
        val lovMeRequest = MedlOppslagRequest(
            fnr = sykepengeSoknad.fnr!!,
            førsteDagForYtelse = sykepengeSoknad.fom!!,
            periode = Periode(sykepengeSoknad.fom, sykepengeSoknad.tom!!),
            brukerinput = Brukerinput(false)
        )
        medlOppslagClient.vurderMedlemskap(lovMeRequest, sykepengeSoknad.sykmeldingId!!)



    }
    suspend fun handle(soknadRecord: SoknadRecord)
    {
        if (validerSoknad(soknadRecord.sykepengeSoknad)) {
            callLovMe(soknadRecord.sykepengeSoknad)
            soknadRecord.logSendt()
        } else {
            soknadRecord.logIkkeSendt()
        }
    }
    private fun SoknadRecord.logIkkeSendt() =
        LovMeService.log.info(
            "Søknad ikke sendt basert på validering - id: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic"
        )

    private fun SoknadRecord.logSendt() =
        LovMeService.log.info(
            "Søknad sendt - id: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic"
        )

    fun validerSoknad(sykepengeSoknad: SykepengeSoknad): Boolean {
        return !sykepengeSoknad.fnr.isNullOrBlank() &&
                !sykepengeSoknad.fom.isNullOrBlank() &&
                !sykepengeSoknad.sykmeldingId.isNullOrBlank()
    }
}
