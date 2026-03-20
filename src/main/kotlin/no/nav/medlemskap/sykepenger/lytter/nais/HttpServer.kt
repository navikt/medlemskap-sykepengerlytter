package no.nav.medlemskap.sykepenger.lytter.nais


import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*

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
import kotlinx.coroutines.withContext
import no.nav.medlemskap.sykepenger.lytter.MDC_CALL_ID
import no.nav.medlemskap.sykepenger.lytter.config.*
import no.nav.medlemskap.sykepenger.lytter.config.JwtConfig.Companion.REALM
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.service.BomloService
import no.nav.medlemskap.sykepenger.lytter.service.FlexMessageHandler
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService

import java.io.Writer
import java.util.*

fun createHttpServer(consumeJob: Job, bomloService: BomloService, env: Map<String, String> = System.getenv()) = embeddedServer(Netty, applicationEngineEnvironment {
    val useAuthentication = true
    val configuration = Configuration()
    val persistenceService = PersistenceService(
        PostgresMedlemskapVurdertRepository(DataSourceBuilder(env).getDataSource()),
        PostgresBrukersporsmaalRepository(DataSourceBuilder(env).getDataSource())
    )
    val flexMessageHandler = FlexMessageHandler(persistenceService)
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
            publiserTestmeldinger(flexMessageHandler, persistenceService)
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

