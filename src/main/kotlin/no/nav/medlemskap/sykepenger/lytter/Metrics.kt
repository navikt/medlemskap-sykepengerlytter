package no.nav.medlemskap.sykepenger.lytter

import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter

object Metrics {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM)

    fun incReceivedTotal(count: Int = 1) =
        receivedTotal.inc(count.toDouble())

    fun incProcessedTotal(count: Int = 1) =
        processedTotal.inc(count.toDouble())

    fun incSuccessfulPenPosts(count: Int = 1) =
        successfulJoarkPosts.inc(count.toDouble())



    private val receivedTotal: Counter = Counter.build()
        .name("medlemskap_joark_lytter_received")
        .help("Totalt mottatte inst meldinger")
        .register()
    private val receivedKilde: Counter = Counter.build()
        .name("medlemskap_joark_lytte_received_kilde")
        .labelNames("kilde")
        .help("Mottatte meldinger per kilde")
        .register()
    private val processedTotal: Counter = Counter.build()
        .name("medlemskap_joark_lytte_processed_counter")
        .help("Totalt prosesserte meldinger")
        .register()
    private val successfulJoarkPosts: Counter = Counter.build()
        .name("medlemskap_joark_lytte_successful_joark_posts_counter")
        .help("Vellykede meldinger sendt til joark")
        .register()
    private val failedJoarkPosts: Counter = Counter.build()
        .name("medlemskap_joark_lytte_failed_joark_posts_counter")
        .labelNames("cause")
        .help("Feilende meldinger sendt til joark")
        .register()

}
