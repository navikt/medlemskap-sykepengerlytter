package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.saga.persistence.*
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.*
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Periode
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.*
import no.nav.medlemskap.sykepenger.lytter.jackson.MedlemskapVurdertParser
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import java.time.LocalDate

class SoknadRecordHandler(
    private val configuration: Configuration,
    private val persistenceService: PersistenceService
) {
    companion object {
        private val log = KotlinLogging.logger { }

    }

    val azureAdClient = AzureAdClient(configuration)
    val restClients = RestClients(
        azureAdClient = azureAdClient,
        configuration = configuration
    )
    var medlOppslagClient: LovmeAPI


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
            val vurdering = callLovMe(soknadRecord.sykepengeSoknad)
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

    private suspend fun callLovMe(sykepengeSoknad: LovmeSoknadDTO) :String{
        var arbeidUtland = getArbeidUtlandFromBrukerSporsmaal(sykepengeSoknad)
        val lovMeRequest = MedlOppslagRequest(
            fnr = sykepengeSoknad.fnr,
            førsteDagForYtelse = sykepengeSoknad.fom.toString(),
            periode = Periode(sykepengeSoknad.fom.toString(), sykepengeSoknad.tom.toString()),
            brukerinput = Brukerinput(arbeidUtland)
        )
        return medlOppslagClient.vurderMedlemskap(lovMeRequest, sykepengeSoknad.id)
    }

     fun getArbeidUtlandFromBrukerSporsmaal(sykepengeSoknad:LovmeSoknadDTO): Boolean {
        // KRAV 1 : er true oppgitt i søknad, bruk denne verdien
        if (sykepengeSoknad.arbeidUtenforNorge==true){
            return true
        }

        if (sykepengeSoknad.arbeidUtenforNorge == null){
            log.info("arbeid utland ikke oppgitt i søknad ${sykepengeSoknad.id}. Setter verdi fra historiske data",
                kv("callId", sykepengeSoknad.id))
        }
        val brukersporsmaal = persistenceService.hentbrukersporsmaalForFnr(sykepengeSoknad.fnr).filter { it.eventDate.isAfter(LocalDate.now().minusYears(1)) }
        val jasvar =  brukersporsmaal.filter { it.sporsmaal?.arbeidUtland ==true }
        val neisvar =  brukersporsmaal.filter { it.sporsmaal?.arbeidUtland ==false }
        val ikkeoppgittsvar = brukersporsmaal.filter { it.sporsmaal?.arbeidUtland ==null }
        //krav 2 : Er det svart JA på tidligere spørsmål, bruk denne verdien
        if (jasvar.isNotEmpty()) {
            log.info("arbeid utland ja oppgitt i tidligere søknader siste året (${jasvar.first().soknadid}) for fnr (kryptert) ${sykepengeSoknad.fnr.sha256()}. Setter arbeid utland lik true",
                kv("callId", sykepengeSoknad.id))
            return true
        }
        //krav 3 : er det svart NEI på tidligere søknader så bruk denne verdien
        if (sykepengeSoknad.arbeidUtenforNorge == null && neisvar.isNotEmpty()){
            log.info("arbeid utland Nei oppgitt i tidligere søknader siste året (${neisvar.first().soknadid}) for fnr (kryptert) ${sykepengeSoknad.fnr.sha256()}. Setter arbeid utland lik false",
                kv("callId", sykepengeSoknad.id))
            return false
        }
        if (sykepengeSoknad.arbeidUtenforNorge == null && ikkeoppgittsvar.isEmpty()){
            log.info("arbeid utland er ikke oppgitt  i søknad ${sykepengeSoknad.id}, og heller aldri oppgitt i tidligere søknader siste året for fnr (kryptert) ${sykepengeSoknad.fnr.sha256()}. Setter arbeid utland lik true")
            return true
        }

        else{
            return false
        }
    }

    fun getRelevanteBrukerSporsmaal(sykepengeSoknad:LovmeSoknadDTO):Brukersporsmaal{
        val listofbrukersporsmaal = persistenceService.hentbrukersporsmaalForFnr(sykepengeSoknad.fnr)
        if (listofbrukersporsmaal.isEmpty()){
            return Brukersporsmaal(fnr = sykepengeSoknad.fnr, soknadid = sykepengeSoknad.id, eventDate = LocalDate.now(), ytelse = "SYKEPENGER", status = "IKKE_SENDT",sporsmaal = FlexBrukerSporsmaal(true))
        }
        val utfortarbeidutenfornorge:Medlemskap_utfort_arbeid_utenfor_norge? =  finnMedlemskap_utfort_arbeid_utenfor_norge(listofbrukersporsmaal)
        val oppholdUtenforNorge:Medlemskap_opphold_utenfor_norge? =  finnMedlemskap_opphold_utenfor_norge(listofbrukersporsmaal)
        val oppholdUtenforEOS: Medlemskap_opphold_utenfor_eos? =  finnMedlemskap_opphold_utenfor_eos(listofbrukersporsmaal)
        val oppholdstilatelse: Medlemskap_oppholdstilatelse_brukersporsmaal? =  finnMMedlemskap_oppholdstilatelse_brukersporsmaal(listofbrukersporsmaal)
        val arbeidUtlandGammelModell = getArbeidUtlandFromBrukerSporsmaal(sykepengeSoknad)
        return Brukersporsmaal(fnr = sykepengeSoknad.fnr,
            soknadid = sykepengeSoknad.id,
            eventDate = LocalDate.now(),
            ytelse = "SYKEPENGER",
            status = "SENDT",
            sporsmaal = FlexBrukerSporsmaal(arbeidUtlandGammelModell),
            oppholdstilatelse=oppholdstilatelse,
            utfort_arbeid_utenfor_norge = utfortarbeidutenfornorge,
            oppholdUtenforNorge = oppholdUtenforNorge,
            oppholdUtenforEOS = oppholdUtenforEOS)
    }




    fun isDuplikat(medlemRequest: Medlemskap): Medlemskap? {
        val vurderinger = persistenceService.hentMedlemskap(medlemRequest.fnr)
        val erFunksjoneltLik = vurderinger.find { medlemRequest.erFunkskjoneltLik(it) }
        return erFunksjoneltLik
    }

    fun isPaafolgendeSoknad(sykepengeSoknad: LovmeSoknadDTO): Boolean {
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
