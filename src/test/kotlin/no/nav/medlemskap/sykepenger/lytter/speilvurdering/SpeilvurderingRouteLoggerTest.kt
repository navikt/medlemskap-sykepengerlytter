package no.nav.medlemskap.sykepenger.lytter.speilvurdering

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.medlemskap.sykepenger.lytter.security.sha256
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class SpeilvurderingRouteLoggerTest {
    @Test
    fun `logger speilrespons med aarsaker og hashet fnr`() {
        val logger = LoggerFactory.getLogger(SpeilvurderingRouteLogger::class.java) as Logger
        val appender = ListAppender<ILoggingEvent>().apply { start() }
        val response = SpeilResponse(
            soknadId = "12345",
            fnr = "test-fnr",
            speilSvar = Speilsvar.UAVKLART,
            aarsaker = listOf(Aarsak("REGEL_5", "Forklaring for regelnummer REGEL_5"))
        )
        logger.addAppender(appender)
        try {
            SpeilvurderingRouteLogger().logVurderingFunnet(response, "call-id")

            val event = appender.list.single()
            val argumenter = event.argumentArray.map { it.toString() }
            assertEquals(
                listOf(
                    response.speilSvar.name,
                    response.fnr.sha256(),
                    "callId=call-id",
                    "fnr=${response.fnr.sha256()}",
                    "endpoint=speilvurdering",
                    "soknadId=12345",
                    "speilSvar=UAVKLART",
                    "aarsaker=${response.aarsaker}"
                ),
                argumenter
            )
            assertTrue(event.markerList.any { it.name == "TEAM_LOGS" })
            assertFalse(event.formattedMessage.contains(response.fnr))
            assertFalse(argumenter.any { it.contains(response.fnr) })
        } finally {
            logger.detachAppender(appender)
            appender.stop()
        }
    }
}
