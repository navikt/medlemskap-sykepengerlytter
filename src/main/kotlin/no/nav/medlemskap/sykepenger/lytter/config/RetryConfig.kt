package no.nav.medlemskap.sykepenger.lytter.config

import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import io.ktor.client.plugins.*
import java.time.Duration

val retryConfig: RetryConfig = RetryConfig
    .custom<RetryConfig>()
    .maxAttempts(3)
    .retryExceptions(RuntimeException::class.java, ResponseException::class.java)
    .waitDuration(Duration.ofSeconds(4))
    .build()

val noRetryConfig: RetryConfig = RetryConfig
    .custom<RetryConfig>()
    .maxAttempts(1)
    .retryExceptions(RuntimeException::class.java)
    .build()

val retryRegistry: RetryRegistry = RetryRegistry.of(retryConfig)
val flexretryRegistry: RetryRegistry = RetryRegistry.of(noRetryConfig)
