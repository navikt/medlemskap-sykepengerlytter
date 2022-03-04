package no.nav.medlemskap.sykepenger.lytter

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import java.util.concurrent.atomic.AtomicInteger

object Metrics {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM)

    fun incReceivedTotal(count: Int = 1) =
        receivedTotal.inc(count.toDouble())

    fun incReceivedvurderingTotal(count: Int = 1) =
        receivedVurderingerTotal.inc(count.toDouble())

    fun incProcessedTotal(count: Int = 1) =
        processedTotal.inc(count.toDouble())

    fun incProcessedVurderingerTotal(count: Int = 1) =
        processedVurderingerTotal.inc(count.toDouble())

    fun incSuccessfulLovmePosts(count: Int = 1) =
        successfulLovmePosts.inc(count.toDouble())

    fun incFailedLovmePosts(count: Int = 1) =
        failedLovmePosts.inc(count.toDouble())


    private val receivedTotal: Counter = Counter.build()
        .name("medlemskap_sykepenger_lytter_received")
        .help("Totalt mottatte inst meldinger")
        .register()
    private val receivedVurderingerTotal: Counter = Counter.build()
        .name("medlemskap_sykepenger_lytter_vurderinger_received")
        .help("Totalt mottatte vurdernger")
        .register()
    private val processedTotal: Counter = Counter.build()
        .name("medlemskap_sykepenger_lytte_processed_counter")
        .help("Totalt prosesserte meldinger")
        .register()

    private val processedVurderingerTotal: Counter = Counter.build()
        .name("medlemskap_sykepenger_lytte_vurderinger_processed_counter")
        .help("Totalt prosesserte vurderinger")
        .register()
    private val successfulLovmePosts: Counter = Counter.build()
        .name("medlemskap_sykepenger_lytte_successful_lovme_posts_counter")
        .help("Vellykede meldinger sendt til lovme")
        .register()
    private val failedLovmePosts: Counter = Counter.build()
        .name("medlemskap_sykepenger_lytte_failed_lovme_posts_counter")
        .labelNames("cause")
        .help("Feilende meldinger sendt til Lovme")
        .register()

    fun clientTimer(service: String?, operation: String?): Timer =
        Timer.builder("client_calls_latency")
            .tags("service", service ?: "UKJENT", "operation", operation ?: "UKJENT")
            .description("latency for calls to other services")
            .publishPercentileHistogram()
            .register(Metrics.globalRegistry)

    fun clientCounter(service: String?, operation: String?, status: String): io.micrometer.core.instrument.Counter =
        io.micrometer.core.instrument.Counter
            .builder("client_calls_total")
            .tags("service", service ?: "UKJENT", "operation", operation ?: "UKJENT", "status", status)
            .description("counter for failed or successful calls to other services")
            .register(Metrics.globalRegistry)

    fun clientsGauge(client: String): AtomicInteger =
        AtomicInteger().apply {
            Gauge.builder("health_check_clients_status") { this }
                .description("Indikerer applikasjonens baksystemers helsestatus. 0 er OK, 1 indikerer feil.")
                .tags("client", client)
                .register(Metrics.globalRegistry)

        }
}
