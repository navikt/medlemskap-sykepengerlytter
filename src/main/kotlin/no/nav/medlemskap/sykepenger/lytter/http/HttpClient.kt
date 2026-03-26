package no.nav.medlemskap.sykepenger.lytter.http

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

private fun ObjectMapper.configureJackson() {
    configure(SerializationFeature.INDENT_OUTPUT, true)
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    setConfig(deserializationConfig.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS))
    setConfig(serializationConfig.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS))
    setDefaultPrettyPrinter(
        DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        }
    )
    registerModule(JavaTimeModule())
}

internal val apacheHttpClient = HttpClient(Apache) {
    this.expectSuccess = true
    install(ContentNegotiation) {
        jackson { configureJackson() }
    }

    engine {
        socketTimeout = 45000

        customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) }
    }
}

internal val cioHttpClient = HttpClient(CIO) {
    this.expectSuccess = true
    install(ContentNegotiation) {
        jackson { configureJackson() }
    }

    engine {
        requestTimeout = 45000
    }
}

internal val httpClient = HttpClient {
    this.expectSuccess = true
    install(ContentNegotiation) {
        jackson { configureJackson() }
    }
}