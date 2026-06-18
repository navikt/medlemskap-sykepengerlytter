package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.LovmeAPI
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.jackson.MedlemskapVurdertParser
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

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
    private val sykepengesoeknadFiltrering = SykepengesoeknadFiltrering(persistenceService)
    private val utledBrukerinput = UtledBrukerinput(persistenceService)

    init {
        medlOppslagClient = restClients.medlOppslag(configuration.register.medlemskapOppslagBaseUrl)

    }


    suspend fun handle(soknadRecord: SoknadRecord) {
        val sykepengeSoknad = soknadRecord.sykepengeSoknad

        when {
            !validerSoknad(sykepengeSoknad) -> soknadRecord.logIkkeSendt()

            sykepengesoeknadFiltrering.finnDuplikatSomSkalFiltreres(sykepengeSoknad) -> log.info(
                "soknad med id ${sykepengeSoknad.id} er funksjonelt lik en annen soknad : kryptertFnr : ${sykepengeSoknad.fnr} ",
                kv("callId", sykepengeSoknad.id)
            )

            sykepengesoeknadFiltrering.lagreHvisPåfølgendeSøknad(sykepengeSoknad) -> log.info(
                "soknad med id ${sykepengeSoknad.id} er påfølgende en annen søknad. Innslag vil bli laget i db, men ingen vurdering vil bli utført ",
                kv("callId", sykepengeSoknad.id)
            )

            else -> when (val resultat = getVurdering(soknadRecord)) {
                is VurderingResultat.Ok -> lagreVurdering(soknadRecord, resultat.vurdering)
                VurderingResultat.SkalIkkeLagres -> {
                    // Gradert adresse eller teknisk feil – allerede logget i getVurdering
                }
            }
        }
    }

    private suspend fun getVurdering(
        soknadRecord: SoknadRecord
    ): VurderingResultat {
        return try {
            val vurdering = utledBrukersvarOgKjørRegelmotor(soknadRecord.sykepengeSoknad)
            soknadRecord.logSendt()
            VurderingResultat.Ok(vurdering)
        } catch (t: Throwable) {
            if (t.message.toString().contains("GradertAdresseException")) {
                log.info("Gradert adresse : key:  ${soknadRecord.key}, offset: ${soknadRecord.offset}")
            } else {
                soknadRecord.logTekniskFeil(t)
            }
            VurderingResultat.SkalIkkeLagres
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

    private suspend fun utledBrukersvarOgKjørRegelmotor(sykepengeSoknad: LovmeSoknadDTO): String {
        val brukerinput = utledBrukerinput.utledBrukerinput(sykepengeSoknad)
        val medlemskapOppslagRequest = MedlemskapOppslagRequestMapper.map(sykepengeSoknad, brukerinput)
        return medlOppslagClient.vurderMedlemskap(medlemskapOppslagRequest, brukerinput.søknadsParametere.callId)
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

private sealed interface VurderingResultat {
    data class Ok(val vurdering: String) : VurderingResultat
    data object SkalIkkeLagres : VurderingResultat
}
