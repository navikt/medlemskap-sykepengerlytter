package no.nav.medlemskap.sykepenger.lytter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.kafka.SykepengeSoeknadKafkaConfig
import no.nav.medlemskap.sykepenger.lytter.nais.createHttpServer
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka.MedlemskapVurdertConsumer
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.kafka.BrukerSporsmaalConsumer
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
        val kafkaConfig = SykepengeSoeknadKafkaConfig(env)
        val brukerSpørsmaalConsumer = BrukerSporsmaalConsumer(
            config = kafkaConfig,
            service = components.sykepengesøknadMottak,
            consumer = kafkaConfig.createFlexConsumer()
        )
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val consumeJob = brukerSpørsmaalConsumer.flow().launchIn(applicationScope)
        medlemskapVurdertConsumer.flow().launchIn(applicationScope)

        try {
            createHttpServer(consumeJob, components).start(wait = true)
        } finally {
            applicationScope.cancel()
            brukerSpørsmaalConsumer.close()
            medlemskapVurdertConsumer.close()
            components.dataSource.close()
        }
    }
}