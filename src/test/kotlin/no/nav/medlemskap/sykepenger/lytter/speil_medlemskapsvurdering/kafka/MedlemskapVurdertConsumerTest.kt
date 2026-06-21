package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import no.nav.medlemskap.sykepenger.lytter.config.FeatureToggleService
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.SpeilResponsBehandler
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class MedlemskapVurdertConsumerTest {

    @Test
    fun `starter kafka-consumer naar feature toggle skrus paa uten restart`() = runBlocking {
        val kafkaConsumer = kafkaConsumerMock()
        val featureToggleService = FeatureToggleServiceMedVerdier(false, true)
        val consumer = MedlemskapVurdertConsumer(
            topic = "topic",
            featureToggleService = featureToggleService,
            consumerFactory = { kafkaConsumer },
            disabledDelayMillis = 1,
        )

        consumer.flow().take(2).toList()

        verify(exactly = 1) { kafkaConsumer.subscribe(listOf("topic")) }
        verify(exactly = 1) { kafkaConsumer.poll(any<Duration>()) }
    }

    @Test
    fun `lukker kafka-consumer naar feature toggle skrus av uten restart`() = runBlocking {
        val kafkaConsumer = kafkaConsumerMock()
        val featureToggleService = FeatureToggleServiceMedVerdier(true, false)
        val consumer = MedlemskapVurdertConsumer(
            topic = "topic",
            featureToggleService = featureToggleService,
            consumerFactory = { kafkaConsumer },
            disabledDelayMillis = 1,
        )

        consumer.flow().take(2).toList()

        verify(exactly = 1) { kafkaConsumer.subscribe(listOf("topic")) }
        verify(exactly = 1) { kafkaConsumer.close() }
    }

    @Test
    fun `oppretter ikke recordHandler foer det finnes records aa behandle`() = runBlocking {
        val kafkaConsumer = kafkaConsumerMock()
        var antallRecordHandlereOpprettet = 0
        val consumer = MedlemskapVurdertConsumer(
            topic = "topic",
            featureToggleService = FeatureToggleServiceMedVerdier(false, true),
            consumerFactory = { kafkaConsumer },
            recordHandlerFactory = {
                antallRecordHandlereOpprettet++
                mockk<SpeilResponsBehandler>(relaxed = true)
            },
            disabledDelayMillis = 1,
        )

        consumer.flow().take(2).toList()

        assertEquals(0, antallRecordHandlereOpprettet)
    }

    private fun kafkaConsumerMock(): KafkaConsumer<String, String> =
        mockk(relaxed = true) {
            every { poll(any<Duration>()) } returns ConsumerRecords.empty()
        }

    private class FeatureToggleServiceMedVerdier(vararg verdier: Boolean) : FeatureToggleService {
        private val verdier = ArrayDeque(verdier.toList())

        override fun isEnabled(toggleName: String, defaultValue: Boolean): Boolean =
            if (verdier.isEmpty()) defaultValue else verdier.removeFirst()
    }
}
