package no.nav.medlemskap.sykepenger.lytter.nais

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
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
import no.nav.medlemskap.sykepenger.lytter.service.BomloService

import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.io.Writer
import java.net.ProxySelector
import java.util.*

fun createHttpServer(consumeJob: Job,bomloService:BomloService) = embeddedServer(Netty, applicationEngineEnvironment {
    val useAuthentication: Boolean = true
    val configuration: Configuration = Configuration()
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

