package no.nav.medlemskap.sykepenger.lytter.config

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

class UnleashFeatureToggleService : FeatureToggleService {

    private val unleash: Unleash = run {
        val apiUrl = System.getenv("UNLEASH_SERVER_API_URL")
            ?.trimEnd('/')
            ?.let { if (it.endsWith("/api")) it else "$it/api" }
            ?: throw IllegalStateException("UNLEASH_SERVER_API_URL er ikke satt")
        val apiToken = System.getenv("UNLEASH_SERVER_API_TOKEN")
            ?: throw IllegalStateException("UNLEASH_SERVER_API_TOKEN er ikke satt")
        val appName = System.getenv("NAIS_APP_NAME") ?: "medlemskap-sykepenger-listener"

        log.info("Initialiserer Unleash mot $apiUrl for app $appName")

        val config = UnleashConfig.builder()
            .appName(appName)
            .instanceId(appName)
            .unleashAPI(apiUrl)
            .customHttpHeader("Authorization", apiToken)
            .build()

        DefaultUnleash(config)
    }

    override fun isEnabled(toggleName: String, defaultValue: Boolean): Boolean =
        unleash.isEnabled(toggleName, defaultValue)
}
