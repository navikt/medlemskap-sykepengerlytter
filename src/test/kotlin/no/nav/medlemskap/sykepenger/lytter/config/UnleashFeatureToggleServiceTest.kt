package no.nav.medlemskap.sykepenger.lytter.config

import no.nav.medlemskap.sykepenger.lytter.feature_toggles.UnleashFeatureToggleService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UnleashFeatureToggleServiceTest {

    @Test
    fun `skal bruke defaultverdi naar unleash-konfigurasjon mangler`() {
        val featureToggleService = UnleashFeatureToggleService(emptyMap())

        assertFalse(featureToggleService.isEnabled("toggle", defaultValue = false))
        assertTrue(featureToggleService.isEnabled("toggle", defaultValue = true))
    }
}
