package no.nav.medlemskap.sykepenger.lytter.feature_toggles

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.config.FeatureToggleService

private val log = KotlinLogging.logger { }

class UnleashFeatureToggleService(
    private val env: Map<String, String> = System.getenv()
) : FeatureToggleService {

    private val unleash: Unleash? = runCatching {
        val apiUrl = env["UNLEASH_SERVER_API_URL"]
            ?.takeIf { it.isNotBlank() }
            ?.trimEnd('/')
            ?.let { if (it.endsWith("/api")) it else "$it/api" }
        val apiToken = env["UNLEASH_SERVER_API_TOKEN"]
            ?.takeIf { it.isNotBlank() }
        val appName = env["NAIS_APP_NAME"] ?: "medlemskap-sykepenger-listener"

        if (apiUrl == null || apiToken == null) {
            log.warn("Unleash-konfigurasjon mangler. Bruker defaultverdi for feature toggles.")
            return@runCatching null
        }

        log.info("Initialiserer Unleash mot $apiUrl for app $appName")

        UnleashConfig.builder()
            .appName(appName)
            .instanceId(appName)
            .unleashAPI(apiUrl)
            .customHttpHeader("Authorization", apiToken)
            .build()
            .let { DefaultUnleash(it) }
    }.getOrElse {
        log.error("Kunne ikke initialisere Unleash. Bruker defaultverdi for feature toggles.", it)
        null
    }

    override fun isEnabled(toggleName: String, defaultValue: Boolean): Boolean =
        unleash?.isEnabled(toggleName, defaultValue) ?: defaultValue
}
