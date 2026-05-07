package no.nav.medlemskap.sykepenger.lytter.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FeatureToggleTest {

    @Test
    fun `NY_FUNKSJONALITET er deaktivert som standard`() {
        // Default i Configuration er "Nei", så togglen skal ikke være aktiv
        assertFalse(FeatureToggle.NY_FUNKSJONALITET.isEnabled())
    }

    @Test
    fun `isEnabled returnerer true når env-variabel er Ja`() {
        withSystemProperty(FeatureToggle.NY_FUNKSJONALITET.envKey, "Ja") {
            assertTrue(FeatureToggle.NY_FUNKSJONALITET.isEnabled())
        }
    }

    @Test
    fun `isEnabled returnerer false når env-variabel er Nei`() {
        withSystemProperty(FeatureToggle.NY_FUNKSJONALITET.envKey, "Nei") {
            assertFalse(FeatureToggle.NY_FUNKSJONALITET.isEnabled())
        }
    }

    /**
     * Setter en systemegenskap midlertidig for testformål, siden [Configuration] leser
     * fra systemeigenskaper (høyeste prioritet i konfig-kjeden).
     */
    private fun withSystemProperty(key: String, value: String, block: () -> Unit) {
        val previous = System.getProperty(key)
        try {
            System.setProperty(key, value)
            block()
        } finally {
            if (previous == null) System.clearProperty(key) else System.setProperty(key, previous)
        }
    }
}
