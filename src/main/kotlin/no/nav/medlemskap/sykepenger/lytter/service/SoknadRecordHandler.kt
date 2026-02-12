package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv

import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.*
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.MedlemskapVurdertParser
import no.nav.medlemskap.sykepenger.lytter.persistence.*
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import org.slf4j.MarkerFactory
import java.time.LocalDate

class SoknadRecordHandler(
    private val configuration: Configuration,
    private val persistenceService: PersistenceService
) {
    companion object {
        private val log = KotlinLogging.logger { }
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    }

    val azureAdClient = AzureAdClient(configuration)
    val restClients = RestClients(
        azureAdClient = azureAdClient,
        configuration = configuration
    )
    var medlOppslagClient: LovmeAPI

    private val finnForrigeBrukersvar = FinnForrigeBrukersvar(persistenceService)
    private val brukersvarGjenbruk = BrukersvarGjenbruk(finnForrigeBrukersvar)

    init {
        medlOppslagClient = restClients.medlOppslag(configuration.register.medlemskapOppslagBaseUrl)

    }


    suspend fun handle(soknadRecord: SoknadRecord) {
        if (validerSoknad(soknadRecord.sykepengeSoknad)) {
            val medlemRequest = mapToMedlemskap(soknadRecord.sykepengeSoknad)
            val duplikat = isDuplikat(medlemRequest)
            if (duplikat != null && arbeidUtenForNorgeFalse(soknadRecord.sykepengeSoknad)) {
                log.info(
                    "soknad med id ${soknadRecord.sykepengeSoknad.id} er funksjonelt lik en annen soknad : kryptertFnr : ${duplikat.fnr} ",
                    kv("callId", soknadRecord.sykepengeSoknad.id)
                )

                return
            } else if (isPaafolgendeSoknad(soknadRecord.sykepengeSoknad)) {
                log.info(
                    "soknad med id ${soknadRecord.sykepengeSoknad.id} er påfølgende en annen søknad. Innslag vil bli laget i db, men ingen vurdering vil bli utført ",
                    kv("callId", soknadRecord.sykepengeSoknad.id)
                )
                return
            } else {
               val  vurdering = getVurdering(soknadRecord)
                if (!vurdering.equals("GradertAdresseException")){
                    lagreVurdering(soknadRecord, vurdering)
                }
            }
        } else {

            soknadRecord.logIkkeSendt()
        }
    }
    private fun lagreVurdering(
        soknadRecord: SoknadRecord,
        vurdering: String
    ) {
        try {
            persistenceService.lagreLovmeResponse(soknadRecord.key!!, MedlemskapVurdertParser().parse(vurdering))
        } catch (t: Exception) {
           log.error("Teknisk feil ved lagring av LovmeRespons i databasen, - sykmeldingId: ${soknadRecord.key} . melding : ${t.message}",
           kv("callId",soknadRecord.key))
        }
    }

    private suspend fun getVurdering(
        soknadRecord: SoknadRecord
    ): String {
        try {
            val vurdering = mapBrukersvarOgKjørRegelmotor(soknadRecord.sykepengeSoknad)
            soknadRecord.logSendt()
            return vurdering
        } catch (t: Throwable) {
            if (t.message.toString().contains("GradertAdresseException")){
                log.info("Gradert adresse : key:  ${soknadRecord.key}, offset: ${soknadRecord.offset}")
                return "GradertAdresseException"
            }
            soknadRecord.logTekniskFeil(t)
            return "GradertAdresseException"
        }
    }

    private fun arbeidUtenForNorgeFalse(sykepengeSoknad: LovmeSoknadDTO): Boolean {
        if(sykepengeSoknad.arbeidUtenforNorge == true) {
            log.info(
            "Søknad inneholder arbeidUtenforNorge=true og skal ikke filtreres - sykmeldingId: ${sykepengeSoknad.id}",
            kv("callId", sykepengeSoknad.id),
        )
        }
        return sykepengeSoknad.arbeidUtenforNorge == false || sykepengeSoknad.arbeidUtenforNorge ==null
    }

    private suspend fun mapBrukersvarOgKjørRegelmotor(sykepengeSoknad: LovmeSoknadDTO): String {
        val søknadsParametere = sykepengeSoknad.tilSøknadsParametere()

        val brukersvarPåSøknad = persistenceService.hentbrukersporsmaalForSoknadID(søknadsParametere.callId)

        log.info(
            teamLogs, "Sjekker om det finnes gjenbrukbare brukersvar for søknad fra sykepengebackend",
            kv("fnr", søknadsParametere.fnr),
            kv("førsteDagForYtelse", søknadsParametere.førsteDagForYtelse)
        )

        val brukerinput = brukersvarGjenbruk.vurderGjenbrukAvBrukersvar(
            søknadsParametere,
            brukersvarPåSøknad
        )

        val medlemskapOppslagRequest = MedlOppslagRequest(
            fnr = søknadsParametere.fnr,
            førsteDagForYtelse = søknadsParametere.førsteDagForYtelse,
            periode = Periode(sykepengeSoknad.fom.toString(), sykepengeSoknad.tom.toString()),
            brukerinput = brukerinput
        )
        return medlOppslagClient.vurderMedlemskap(medlemskapOppslagRequest, søknadsParametere.callId)
    }

    fun isDuplikat(medlemRequest: Medlemskap): Medlemskap? {
        val vurderinger = persistenceService.hentMedlemskap(medlemRequest.fnr)
        val erFunksjoneltLik = vurderinger.find { medlemRequest.erFunkskjoneltLik(it) }
        return erFunksjoneltLik
    }

    fun isPaafolgendeSoknad(sykepengeSoknad: LovmeSoknadDTO): Boolean {
        if (true ==  sykepengeSoknad.forstegangssoknad){
            return false
        }
        val medlemRequest = mapToMedlemskap(sykepengeSoknad)
        val vurderinger = persistenceService.hentMedlemskap(sykepengeSoknad.fnr)
        val result = vurderinger.find { medlemRequest.erpåfølgende(it) }
        if (result != null && arbeidUtenForNorgeFalse(sykepengeSoknad)) {
            persistenceService.lagrePaafolgendeSoknad(sykepengeSoknad)
            return true
        }
        return false
    }

    private fun mapToMedlemskap(sykepengeSoknad: LovmeSoknadDTO): Medlemskap {
        return Medlemskap(sykepengeSoknad.fnr, sykepengeSoknad.fom!!, sykepengeSoknad.tom!!, ErMedlem.UAVKLART)

    }

    private fun SoknadRecord.logIkkeSendt() =
        log.info(
            "Søknad ikke  sendt til lovme basert på validering - sykmeldingId: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", sykepengeSoknad.id),
        )

    private fun SoknadRecord.logSendt() =
        log.info(
            "Søknad videresendt til Lovme - sykmeldingId: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic",
            kv("callId", sykepengeSoknad.id),
        )

    private fun SoknadRecord.logTekniskFeil(t: Throwable) =
        log.info(
            "Teknisk feil ved kall mot LovMe - sykmeldingId: ${sykepengeSoknad.id}, melding:" + t.message,
            kv("callId", sykepengeSoknad.id),
        )

    fun validerSoknad(sykepengeSoknad: LovmeSoknadDTO): Boolean {
        return !sykepengeSoknad.fnr.isNullOrBlank() &&
                !sykepengeSoknad.id.isNullOrBlank()

    }
}

fun LovmeSoknadDTO.tilSøknadsParametere(): SoeknadsParametere =
    SoeknadsParametere(
        callId = id,
        fnr = fnr,
        førsteDagForYtelse = fom.toString()
    )
