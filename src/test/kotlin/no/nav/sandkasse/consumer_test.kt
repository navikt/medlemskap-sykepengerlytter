package no.nav.sandkasse


import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.config.KafkaConfig
import no.nav.medlemskap.sykepenger.lytter.config.PlainStrategy
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer

import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun main(args: Array<String>) {
    val securityStrategy: KafkaConfig.SecurityStrategy = PlainStrategy(environment = System.getenv())
    val value = 1652174197864
    val date = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(value), ZoneId.systemDefault())
    println(date)

    val consumer: KafkaConsumer<String, String> = KafkaConsumer<String, String>(mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to Configuration.KafkaConfig().bootstrapServers,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        CommonClientConfigs.CLIENT_ID_CONFIG to "client_id",
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.GROUP_ID_CONFIG to "medlemskap.sandkasse",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 10,

        ) + securityStrategy.securityConfig())
    val flex_topic = "flex.sykepengesoknad"

    consumer.subscribe(listOf(flex_topic))

    val pollTimeout = Duration.ofSeconds(2)
    while (true) {
        val records = consumer.poll(pollTimeout)

        if (!records.isEmpty) {
            records.map { transform(it) }
            records.forEach {
                handleMessage(it)
            }

            //consumer.commitSync()
        } else {
            println("ingen meldinger funnet på kø")
        }
    }
}

fun transform(it: ConsumerRecord<String, String>?) {

}

fun handleMessage(it: ConsumerRecord<String, String>?) {
    val timestamp = LocalDateTime.ofInstant(
        it?.let { it1 -> Instant.ofEpochMilli(it1.timestamp()) }, ZoneId.systemDefault())

    val json = it?.value()
    val JsonNode = ObjectMapper().readTree(json)
    val sporsmålArray = JsonNode.get("sporsmal")
    val fnr = JsonNode.get("fnr").asText()
    val status = JsonNode.get("status").asText()
    val type = JsonNode.get("type").asText()
    val id = JsonNode.get("id").asText()
    var svar:String ="IKKE OPPGITT"
    if (status==Soknadstatus.SENDT.toString()){
        val arbeidutland = sporsmålArray.find { it.get("tag").asText().equals("ARBEID_UTENFOR_NORGE") }
        if (arbeidutland != null){
            //println(arbeidutland)
            try {
                svar = arbeidutland.get("svar").get(0).get("verdi").asText()
            }
            catch (t:Throwable){

            }
        }
        else{
            println(json)
        }

        println("$timestamp, $id , $fnr , $status , $type , $svar")
    }

    //println(it.value())

}
enum class Soknadstatus {
    NY,
    SENDT,
    FREMTIDIG,
    UTKAST_TIL_KORRIGERING,
    KORRIGERT,
    AVBRUTT,
    UTGATT,
    SLETTET
}
