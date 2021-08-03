package no.nav.medlemskap.sykepenger.lytter

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import no.nav.medlemskap.sykepenger.lytter.config.Environment
import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun main() {
    Application().start()
}

class Application(private val env: Environment = System.getenv(), private val consumer: Consumer = Consumer(env)) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(Application::class.java)
    }

    fun start() {
        log.info("Start application")

        @OptIn(DelicateCoroutinesApi::class)
        val consumeJob = consumer.flow().launchIn(GlobalScope)

        naisLiveness(consumeJob).start(wait = true)
    }
}