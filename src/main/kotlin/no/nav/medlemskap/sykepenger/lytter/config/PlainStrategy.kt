package no.nav.medlemskap.sykepenger.lytter.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol

internal class PlainStrategy(private val environment: Environment) :
    KafkaConfig.SecurityStrategy {
    private val isLocal = EnvironmentKey.IS_LOCAL.equals("true")

    override fun securityConfig() = mapOf(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to SecurityProtocol.SASL_SSL.name,
        SaslConfigs.SASL_MECHANISM to "PLAIN",
    ) + trustStoreConfig()

    private fun trustStoreConfig(): Map<String, String> =
        if (isLocal) {
            System.setProperty("javax.net.ssl.trustStore", Configuration.KafkaConfig().trustStorePath)
            mapOf(
                SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to Configuration.KafkaConfig().trustStorePath,
                SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to Configuration.KafkaConfig().keystorePassword
            )
        } else {
            mapOf(
                SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to Configuration.KafkaConfig().keystoreLocation,
                SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to Configuration.KafkaConfig().keystorePassword,
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to Configuration.KafkaConfig().securityProtocol,
                CommonClientConfigs.CLIENT_ID_CONFIG to Configuration.KafkaConfig().clientId,
                SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to Configuration.KafkaConfig().trustStorePath,
                SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to Configuration.KafkaConfig().keystorePassword,
                SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to Configuration.KafkaConfig().keystoreType

            )
        }

}