package no.nav.medlemskap.sykepenger.lytter.nais

import io.ktor.http.HttpStatusCode
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CancellationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HttpServerStatusPagesTest {

    @Test
    fun `uventet exception gir 500 uten feildetaljer`() = testApplication {
        application {
            configureStatusPages()
            routing {
                get("/test") {
                    error("intern feildetalj")
                }
            }
        }

        val response = client.get("/test")

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertFalse(response.bodyAsText().contains("intern feildetalj"))
    }

    @Test
    fun `CancellationException propagere`() = testApplication {
        application {
            configureStatusPages()
            routing {
                get("/test") {
                    throw CancellationException("request cancelled")
                }
            }
        }

        var cancellationPropagated = false
        try {
            client.get("/test")
        } catch (_: CancellationException) {
            cancellationPropagated = true
        }
        assertTrue(cancellationPropagated)
    }
}
