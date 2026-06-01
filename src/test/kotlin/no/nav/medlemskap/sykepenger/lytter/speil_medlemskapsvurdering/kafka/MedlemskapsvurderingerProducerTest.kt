package no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.kafka

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.SpeilRespons
import no.nav.medlemskap.sykepenger.lytter.speil_medlemskapsvurdering.Speilsvar
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

class MedlemskapsvurderingerProducerTest {

    @Test
    fun `publiserer SpeilRespons med soknadId som key`() {
        val topic = "medlemskap.medlemskapsvurderinger"
        val recordSlot = slot<ProducerRecord<String, String>>()
        val producer = mockk<Producer<String, String>>()
        every { producer.send(capture(recordSlot)) } returns CompletableFuture.completedFuture(
            RecordMetadata(TopicPartition(topic, 0), 0L, 0, 0L, 0, 0)
        )
        val speilRespons = SpeilRespons("soknad-1", "fnr-1", Speilsvar.UAVKLART)

        MedlemskapsvurderingerProducer(topic = topic, producer = producer).publiser(speilRespons)

        Assertions.assertEquals(topic, recordSlot.captured.topic())
        Assertions.assertEquals("soknad-1", recordSlot.captured.key())
        Assertions.assertEquals(
            speilRespons,
            objectMapper.readValue(recordSlot.captured.value(), SpeilRespons::class.java)
        )
        verify(exactly = 1) { producer.send(any<ProducerRecord<String, String>>()) }
    }
}