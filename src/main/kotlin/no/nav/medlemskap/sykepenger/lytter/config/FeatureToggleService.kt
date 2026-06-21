package no.nav.medlemskap.sykepenger.lytter.config

interface FeatureToggleService {
    fun isEnabled(toggleName: String, defaultValue: Boolean = false): Boolean
}
