package no.nav.medlemskap.sykepenger.lytter.nais


import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.response.respond

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.metrics.micrometer.*

import org.slf4j.event.Level
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.exporter.common.TextFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.MDC_CALL_ID
import no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus.FinnMedlemskapsstatus
import no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus.MedlemskapsstatusService
import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.HentGjenbrukbareBrukerspoersmaal
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.LagFlexRespons
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.brukerSporsmaalRoute
import no.nav.medlemskap.sykepenger.lytter.config.*
import no.nav.medlemskap.sykepenger.lytter.config.JwtConfig.Companion.REALM
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.security.AuthorizationHandler
import no.nav.medlemskap.sykepenger.lytter.service.BomloService
import no.nav.medlemskap.sykepenger.lytter.service.GjenbrukBrukersvar
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.service.TidligereBrukersvar
import no.nav.medlemskap.sykepenger.lytter.service.UtledBrukerinput
import no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus.medlemskapsstatusRoute
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadMottak
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.BehandleSykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.LagreVurderingsstatus
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.SykepengesoeknadFiltrering
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.LagreBrukerspoersmaal

import java.io.Writer
import java.util.*

private val logger = KotlinLogging.logger { }

fun createHttpServer(consumeJob: Job, bomloService: BomloService, env: Map<String, String> = System.getenv()) = embeddedServer(Netty, applicationEngineEnvironment {
    val useAuthentication = true
    val authorizationHandler = AuthorizationHandler()
    val configuration = Configuration()
    val persistenceService = PersistenceService(
        PostgresMedlemskapVurdertRepository(DataSourceBuilder(env).getDataSource()),
        PostgresBrukersporsmaalRepository(DataSourceBuilder(env).getDataSource())
    )
    val sagaClient = RestClients(
        azureAdClient = AzureAdClient(configuration),
        configuration = configuration
    ).saga(configuration.register.medlemskapSagaBaseUrl)
    val finnMedlemskapsstatus = FinnMedlemskapsstatus(
        persistenceService,
        MedlemskapsstatusService(sagaClient)
    )
    val medlemskapOppslagService = MedlemskapOppslagService(configuration)
    val tidligereBrukersvar = TidligereBrukersvar(persistenceService)
    val gjenbrukBrukersvar = GjenbrukBrukersvar(tidligereBrukersvar)
    val lagFlexRespons = LagFlexRespons(HentGjenbrukbareBrukerspoersmaal(tidligereBrukersvar))

    //denne opprettes her fordi den brukes i routen publiserTestmeldinger til testrammeverket
    val sykepengesøknadMottak = SykepengesoeknadMottak(
        behandleSykepengesøknad = BehandleSykepengesoeknad(
            filtrering = SykepengesoeknadFiltrering(persistenceService),
            utledBrukerinput = UtledBrukerinput(gjenbrukBrukersvar),
            lagreVurderingsstatus = LagreVurderingsstatus(persistenceService),
            medlemskapOppslagService = medlemskapOppslagService
        ),
        lagreBrukerspoersmaal = LagreBrukerspoersmaal(persistenceService)
    )
    val azureAdOpenIdConfiguration: AzureAdOpenIdConfiguration = getAadConfig(configuration.azureAd)

    connector { port = 8080 }
    module {

        install(CallId) {
            header(MDC_CALL_ID)
            generate { UUID.randomUUID().toString() }
        }

        install(CallLogging) {
            level = Level.INFO
            callIdMdc(MDC_CALL_ID)
        }

        install(MicrometerMetrics) {
            registry = Metrics.registry
        }
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(objectMapper))
        }

        install(StatusPages) {
            exception<ContentTransformationException> { call, cause ->
                logger.warn(cause) {
                    "Ugyldig request, callId=${call.callId}"
                }
                call.respond(HttpStatusCode.BadRequest)
            }
            exception<Exception> { call, cause ->
                if (cause is CancellationException) {
                    throw cause
                }
                logger.error(cause) {
                    "Uventet feil, callId=${call.callId}"
                }
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        if (useAuthentication) {
            //logger.info { "Installerer authentication" }
            install(Authentication) {
                jwt("azureAuth") {
                    val jwtConfig = JwtConfig(configuration, azureAdOpenIdConfiguration)
                    realm = REALM
                    verifier(jwtConfig.jwkProvider, azureAdOpenIdConfiguration.issuer)
                    validate { credentials ->
                        jwtConfig.validate(credentials)
                    }
                }
            }
        } else {
            //logger.info { "Installerer IKKE authentication" }
        }

        routing {
            naisRoutes(consumeJob,bomloService)
            sykepengerLytterRoutes(bomloService)
            medlemskapsstatusRoute(finnMedlemskapsstatus)
            brukerSporsmaalRoute(authorizationHandler, medlemskapOppslagService, lagFlexRespons)
            publiserTestmeldinger(sykepengesøknadMottak, persistenceService)
        }
    }
})

suspend fun writeMetrics004(writer: Writer, registry: PrometheusMeterRegistry) {
    withContext(Dispatchers.IO) {
        kotlin.runCatching {
            TextFormat.write004(writer, registry.prometheusRegistry.metricFamilySamples())
        }
    }
}
