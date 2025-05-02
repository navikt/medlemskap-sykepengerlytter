package no.nav.medlemskap.sykepenger.lytter.http

import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.github.resilience4j.retry.Retry
import io.ktor.client.plugins.*
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.CancellationException
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.nais.Metrics.clientCounter
import no.nav.medlemskap.sykepenger.lytter.nais.Metrics.clientTimer


private val logger = KotlinLogging.logger { }

suspend fun <T> runWithRetryAndMetrics(service: String, operation: String, retry: Retry?, block: suspend () -> T): T {
    try {
        retry?.let {
            return it.executeSuspendFunction {
                runWithMetrics(service, operation, block)
            }
        }
        return runWithMetrics(service, operation, block)
    } catch (jce: CancellationException) {
        logger.info("Kall mot $service:$operation kanselleres pga feil i kall mot annet baksystem", jce)
        throw jce
    }
    catch (cause: ResponseException){
        if (cause.response.status.value == 404) {
            throw cause
        }
        else{
            logger.warn("Feilet under kall mot $service:$operation : ${cause.message}", cause)
            throw cause
        }
    }
    catch (t: Throwable) {
        logger.warn("Feilet under kall mot $service:$operation : ${t.message}", t)
        throw t
    }
}
suspend fun <T> runWithRetryAndMetrics(service: String, operation: String, retry: Retry?,callId:String, block: suspend () -> T): T {
    try {
        retry?.let {
            return it.executeSuspendFunction {
                runWithMetrics(service, operation, block)
            }
        }
        return runWithMetrics(service, operation, block)
    } catch (jce: CancellationException) {
        logger.info("Kall mot $service:$operation kanselleres pga feil i kall mot annet baksystem", jce,
            kv("callId",callId)
        )
        throw jce
    }
    catch (cause: ResponseException){
        if (cause.response.status.value == 404) {
            throw cause
        }
        else{
            logger.warn("Feilet under kall mot $service:$operation : ${cause.message}", cause,
                kv("callId",callId)
            )
            throw cause
        }
    }
    catch (t: Throwable) {
        logger.warn("Feilet under kall mot $service:$operation : ${t.message}", t,
            kv("callId",callId))
        throw t
    }
}

suspend fun <T> runWithMetrics(service: String, operation: String, block: suspend () -> T): T {
    var timer: Timer.Sample? = null
    try {
        timer = Timer.start()
    } catch (t: Throwable) {
        logger.warn("Feilet under opprettelsen av timer for $service:$operation", t)
    }
    try {
        val result: T = block.invoke()
        try {
            clientCounter(service, operation, "success").increment()
        } catch (t: Throwable) {
            logger.warn("Feilet under inkrementinger av counter for $service:$operation", t)
        }
        return result
    } catch (t: Throwable) {
        try {
            clientCounter(service, operation, "failure").increment()
        } catch (t: Throwable) {
            logger.warn("Feilet under inkrementinger av counter for $service:$operation", t)
        }
        throw t
    } finally {
        try {
            timer?.let { it.stop(clientTimer(service, operation)) }
        } catch (t: Throwable) {
            logger.warn("Feilet under loggingen av timer for $service:$operation", t)
        }
    }
}
