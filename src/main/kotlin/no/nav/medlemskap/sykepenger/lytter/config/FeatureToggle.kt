package no.nav.medlemskap.sykepenger.lytter.config

import mu.KotlinLogging

private val log = KotlinLogging.logger { }

/**
 * Feature toggles for parallel development.
 *
 * New toggles must:
 *  1. Be added as an enum entry with its corresponding environment-variable key.
 *  2. Have the env-var added to [Configuration.defaultProperties] (default "Nei").
 *  3. Have the env-var declared in `.nais/dev.yaml` and `.nais/prod.yaml`.
 *
 * Usage:
 *   if (FeatureToggle.NY_FUNKSJONALITET.isEnabled()) { ... }
 */
enum class FeatureToggle(val envKey: String) {
    NY_FUNKSJONALITET("NY_FUNKSJONALITET_ENABLED");

    fun isEnabled(): Boolean {
        val enabled = Configuration.FeatureToggles().toggles[this] == "Ja"
        log.debug { "Feature toggle $name (${envKey}): $enabled" }
        return enabled
    }
}
