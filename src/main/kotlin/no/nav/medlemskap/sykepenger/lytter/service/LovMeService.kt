package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*

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

    suspend fun callLovMe(sykepengeSoknad: LovmeSoknadDTO)
    {
        val lovMeRequest = MedlOppslagRequest(
            fnr = sykepengeSoknad.fnr,
            førsteDagForYtelse = sykepengeSoknad.fom.toString(),
            periode = Periode(sykepengeSoknad.fom.toString(), sykepengeSoknad.tom?.toString()),
            brukerinput = Brukerinput(false)
        )
        medlOppslagClient.vurderMedlemskap(lovMeRequest, sykepengeSoknad.id)



    }
    suspend fun handle(soknadRecord: SoknadRecord)
    {
        if (validerSoknad(soknadRecord.sykepengeSoknad)) {
            try {
                callLovMe(soknadRecord.sykepengeSoknad)
                soknadRecord.logSendt()
            }
            catch (t:Throwable){
                soknadRecord.logTekiskFeil(t)
            }
        } else {
            soknadRecord.logIkkeSendt()
        }
    }
    private fun SoknadRecord.logIkkeSendt() =
        LovMeService.log.info(
            "Søknad ikke  sendt til lovme basert på validering - sykmeldingId: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", sykepengeSoknad.id),
        )

    private fun SoknadRecord.logSendt() =
        LovMeService.log.info(
            "Søknad videresendt til Lovme - sykmeldingId: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", sykepengeSoknad.id),
        )
    private fun SoknadRecord.logTekiskFeil(t:Throwable) =
        LovMeService.log.info(
            "Teknisk feil ved kall mot LovMe - sykmeldingId: ${sykepengeSoknad.id}, melding:"+t.message,
            kv("callId", sykepengeSoknad.id),
        )

    fun validerSoknad(sykepengeSoknad: LovmeSoknadDTO): Boolean {
        return !sykepengeSoknad.fnr.isNullOrBlank() &&
                !sykepengeSoknad.id.isNullOrBlank()
    }
}
