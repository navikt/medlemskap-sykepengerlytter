package no.nav.medlemskap.sykepenger.lytter.config

import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import java.time.Duration

val retryConfig: RetryConfig = RetryConfig
    .custom<RetryConfig>()
    .maxAttempts(3)
    .waitDuration(Duration.ofSeconds(3))
    .retryExceptions(RuntimeException::class.java)
    .build()

val retryRegistry: RetryRegistry = RetryRegistry.of(retryConfig)
