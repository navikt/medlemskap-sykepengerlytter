package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.security.AuthorizationHandler
import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService
import no.nav.medlemskap.sykepenger.lytter.service.TidligereBrukersvar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BrukersporsmaalRouteHentNyesteBrukersvarTest {

    private val hemmelighet = "test-hemmelighet"
    private val fnr = "12345678910"

    private fun gyldigToken(azp: String = "test-klient"): String =
        JWT.create()
            .withIssuer("test-issuer")
            .withAudience("test-audience")
            .withClaim("azp", azp)
            .sign(Algorithm.HMAC256(hemmelighet))

    private fun installTestApp(
        tidligereBrukersvar: TidligereBrukersvar,
        application: io.ktor.server.application.Application,
        erDevMiljø: Boolean = true
    ) {
        application.apply {
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(objectMapper))
            }
            install(Authentication) {
                jwt("azureAuth") {
                    verifier(JWT.require(Algorithm.HMAC256(hemmelighet)).build())
                    validate { credentials -> JWTPrincipal(credentials.payload) }
                }
            }
            routing {
                brukerSporsmaalRoute(
                    authorizationHandler = AuthorizationHandler(),
                    medlemskapOppslagService = mockk<MedlemskapOppslagService>(relaxed = true),
                    lagFlexRespons = mockk<LagFlexRespons>(relaxed = true),
                    tidligereBrukersvar = tidligereBrukersvar,
                    erDevMiljø = erDevMiljø
                )
            }
        }
    }

    @Test
    fun `returnerer 200 med nyeste brukersvar når det finnes`() = testApplication {
        val forventetBrukersvar = Brukerspørsmål(
            fnr = fnr,
            soknadid = "soknad-1",
            eventDate = LocalDate.of(2024, 1, 15),
            ytelse = "SYKEPENGER",
            status = "SENDT",
            sporsmaal = null
        )
        val tidligereBrukersvar = mockk<TidligereBrukersvar>()
        every { tidligereBrukersvar.finnNyesteBrukersvar(fnr) } returns forventetBrukersvar

        application { installTestApp(tidligereBrukersvar, this) }

        val response = client.post("/hentNyesteBrukersvar") {
            header(HttpHeaders.Authorization, "Bearer ${gyldigToken()}")
            contentType(ContentType.Application.Json)
            setBody("""{"fnr":"$fnr"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("soknad-1"))
    }

    @Test
    fun `returnerer 204 når ingen brukersvar finnes`() = testApplication {
        val tidligereBrukersvar = mockk<TidligereBrukersvar>()
        every { tidligereBrukersvar.finnNyesteBrukersvar(fnr) } returns null

        application { installTestApp(tidligereBrukersvar, this) }

        val response = client.post("/hentNyesteBrukersvar") {
            header(HttpHeaders.Authorization, "Bearer ${gyldigToken()}")
            contentType(ContentType.Application.Json)
            setBody("""{"fnr":"$fnr"}""")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `returnerer 400 når fnr mangler i body`() = testApplication {
        val tidligereBrukersvar = mockk<TidligereBrukersvar>(relaxed = true)

        application { installTestApp(tidligereBrukersvar, this) }

        val response = client.post("/hentNyesteBrukersvar") {
            header(HttpHeaders.Authorization, "Bearer ${gyldigToken()}")
            contentType(ContentType.Application.Json)
            setBody("""{"fnr":""}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `returnerer 401 uten token`() = testApplication {
        val tidligereBrukersvar = mockk<TidligereBrukersvar>(relaxed = true)

        application { installTestApp(tidligereBrukersvar, this) }

        val response = client.post("/hentNyesteBrukersvar") {
            contentType(ContentType.Application.Json)
            setBody("""{"fnr":"$fnr"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `endepunktet finnes ikke utenfor dev-miljø`() = testApplication {
        val tidligereBrukersvar = mockk<TidligereBrukersvar>(relaxed = true)

        application { installTestApp(tidligereBrukersvar, this, erDevMiljø = false) }

        val response = client.post("/hentNyesteBrukersvar") {
            header(HttpHeaders.Authorization, "Bearer ${gyldigToken()}")
            contentType(ContentType.Application.Json)
            setBody("""{"fnr":"$fnr"}""")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
