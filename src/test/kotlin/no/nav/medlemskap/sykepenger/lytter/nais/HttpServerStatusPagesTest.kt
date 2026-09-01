package no.nav.medlemskap.sykepenger.lytter.nais

import io.ktor.http.HttpStatusCode
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.routing.get
import io.ktor.server.routing.post
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
    fun `CancellationException propagerer`() = testApplication {
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

    @Test
    fun `ugyldig json gir 400`() = testApplication {
        application {
            install(ContentNegotiation) {
                jackson()
            }
            configureStatusPages()
            routing {
                post("/test") {
                    call.receive<TestRequest>()
                }
            }
        }

        val response = client.post("/test") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "blabla": "31878699486",
                  "førsteDagForYtelse": "2026-08-01",
                  "periode": {
                    "fom": "2026-08-03",
                    "tom": "2026-08-21"
                  },
                  "ytelse": "SYKEPENGER"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    private data class TestRequest(val fnr: String)
}
