package no.nav.medlemskap.sykepenger.lytter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.kafka.SykepengesoeknadKafkaConfig
import no.nav.medlemskap.sykepenger.lytter.nais.createHttpServer
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka.MedlemskapVurdertConsumer
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.kafka.SykepengesoeknadConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun main() {
    Application().start()
}

class Application(private val env: Environment = System.getenv(),
                  private val medlemskapVurdertConsumer: MedlemskapVurdertConsumer = MedlemskapVurdertConsumer()
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(Application::class.java)
    }

    fun start() {
        log.info("Start application")
        val components = ApplicationComponents.create(env)
        DataSourceBuilder(env).migrate(components.dataSource)
        val kafkaConfig = SykepengesoeknadKafkaConfig(env)
        val sykepengesøknadConsumer = SykepengesoeknadConsumer(
            config = kafkaConfig,
            service = components.sykepengesøknad.mottak,
            consumer = kafkaConfig.createFlexConsumer()
        )
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val consumeJob = sykepengesøknadConsumer.flow().launchIn(applicationScope)
        val medlemskapVurdertJob = medlemskapVurdertConsumer.flow().launchIn(applicationScope)

        try {
            createHttpServer(consumeJob, components).start(wait = true)
        } finally {
            sykepengesøknadConsumer.stop()
            medlemskapVurdertConsumer.stop()
            runBlocking {
                listOf(consumeJob, medlemskapVurdertJob).joinAll()
            }
            applicationScope.cancel()
            sykepengesøknadConsumer.close()
            medlemskapVurdertConsumer.close()
            components.dataSource.close()
        }
    }
}