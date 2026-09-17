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
import io.ktor.server.plugins.BadRequestException
import io.ktor.serialization.JsonConvertException
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
import no.nav.medlemskap.sykepenger.lytter.ApplicationComponents
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.brukerSporsmaalRoute
import no.nav.medlemskap.sykepenger.lytter.config.*
import no.nav.medlemskap.sykepenger.lytter.config.JwtConfig.Companion.REALM
import no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus.medlemskapsstatusRoute
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.speilvurderingRoute

import java.io.Writer
import java.util.*

private val logger = KotlinLogging.logger { }

internal fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ContentTransformationException> { call, cause ->
            logger.warn(cause) {
                "Ugyldig request, callId=${call.callId}"
            }
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<JsonConvertException> { call, cause ->
            logger.warn(cause) {
                "Ugyldig request, callId=${call.callId}"
            }
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<BadRequestException> { call, cause ->
            logger.warn(cause) {
                "Ugyldig request, callId=${call.callId}"
            }
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<CancellationException> { _, cause ->
            throw cause
        }
        exception<Exception> { call, cause ->
            logger.error(cause) {
                "Uventet feil, callId=${call.callId}"
            }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

fun createHttpServer(consumeJob: Job, components: ApplicationComponents) = embeddedServer(Netty, applicationEngineEnvironment {
    val useAuthentication = true
    val azureAdOpenIdConfiguration: AzureAdOpenIdConfiguration = getAadConfig(components.configuration.azureAd)

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

        configureStatusPages()

        if (useAuthentication) {
            //logger.info { "Installerer authentication" }
            install(Authentication) {
                jwt("azureAuth") {
                    val jwtConfig = JwtConfig(components.configuration, azureAdOpenIdConfiguration)
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
            naisRoutes(consumeJob, components.hentEllerOpprettVurdering)
            speilvurderingRoute(
                hentEllerOpprettVurdering = components.hentEllerOpprettVurdering,
                speilvurderingMapper = components.speilvurderingMapper
            )
            medlemskapsstatusRoute(components.finnMedlemskapsstatus)
            brukerSporsmaalRoute(
                components.authorizationHandler,
                components.medlemskapOppslagService,
                components.lagFlexRespons
            )
            testrammeverkRoutes(
                components.sykepengesøknadMottak,
                components.persistenceService,
                components.authorizationHandler,
                components.testrammeverkService
            )
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
