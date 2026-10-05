package no.nav.medlemskap.sykepenger.lytter.nais

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Metrics as MicrometerMetrics
import io.micrometer.core.instrument.Timer
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.util.concurrent.atomic.AtomicInteger

object Metrics {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    fun incReceivedTotal(count: Int = 1) =
        receivedTotal.increment(count.toDouble())

    fun incReceivedvurderingTotal(count: Int = 1) =
        receivedVurderingerTotal.increment(count.toDouble())

    fun incProcessedTotal(count: Int = 1) =
        processedTotal.increment(count.toDouble())

    fun incProcessedVurderingerTotal(count: Int = 1) =
        processedVurderingerTotal.increment(count.toDouble())

    fun incSuccessfulLovmePosts(count: Int = 1) =
        successfulLovmePosts.increment(count.toDouble())

    fun incFailedLovmePosts(count: Int = 1) =
        incFailedLovmePosts("UKJENT", count)

    fun incFailedLovmePosts(cause: String, count: Int = 1) =
        Counter.builder("medlemskap_sykepenger_lytte_failed_lovme_posts_counter")
            .tag("cause", cause)
            .description("Feilende meldinger sendt til Lovme")
            .register(registry)
            .increment(count.toDouble())

    private val receivedTotal = Counter.builder("medlemskap_sykepenger_lytter_received")
        .description("Totalt mottatte inst meldinger")
        .register(registry)
    private val receivedVurderingerTotal = Counter.builder("medlemskap_sykepenger_lytter_vurderinger_received")
        .description("Totalt mottatte vurdernger")
        .register(registry)
    private val processedTotal = Counter.builder("medlemskap_sykepenger_lytte_processed_counter")
        .description("Totalt prosesserte meldinger")
        .register(registry)

    private val processedVurderingerTotal = Counter.builder("medlemskap_sykepenger_lytte_vurderinger_processed_counter")
        .description("Totalt prosesserte vurderinger")
        .register(registry)
    private val successfulLovmePosts = Counter.builder("medlemskap_sykepenger_lytte_successful_lovme_posts_counter")
        .description("Vellykede meldinger sendt til lovme")
        .register(registry)

    fun clientTimer(service: String?, operation: String?): Timer =
        Timer.builder("client_calls_latency")
            .tags("service", service ?: "UKJENT", "operation", operation ?: "UKJENT")
            .description("latency for calls to other services")
            .publishPercentileHistogram()
            .register(MicrometerMetrics.globalRegistry)

    fun clientCounter(service: String?, operation: String?, status: String): io.micrometer.core.instrument.Counter =
        io.micrometer.core.instrument.Counter
            .builder("client_calls_total")
            .tags("service", service ?: "UKJENT", "operation", operation ?: "UKJENT", "status", status)
            .description("counter for failed or successful calls to other services")
            .register(MicrometerMetrics.globalRegistry)

    fun clientsGauge(client: String): AtomicInteger =
        AtomicInteger().apply {
            Gauge.builder("health_check_clients_status") { this }
                .description("Indikerer applikasjonens baksystemers helsestatus. 0 er OK, 1 indikerer feil.")
                .tags("client", client)
                .register(MicrometerMetrics.globalRegistry)

        }
}
