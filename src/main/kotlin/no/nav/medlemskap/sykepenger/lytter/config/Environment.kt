package no.nav.medlemskap.sykepenger.lytter.config

typealias Environment = Map<String, String>

enum class EnvironmentKey(val key: String) {
    SERVICE_USER_USERNAME("SERVICE_USER_USERNAME"),
    SERVICE_USER_PASSWORD("SERVICE_USER_PASSWORD"),
    SSL_TRUSTSTORE_LOCATION("NAV_TRUSTSTORE_PATH"),
    SSL_TRUSTSTORE_PASSWORD("NAV_TRUSTSTORE_PASSWORD"),
    IS_LOCAL("IS_LOCAL"),
    BOOTSTRAP_SERVERS("KAFKA_BOOTSTRAP_SERVERS"),
    SCHEMA_REGISTRY("SCHEMA_REGISTRY_URL"),
    INST_TOPIC("INST_TOPIC"),
    INST_GROUP("INST_GROUP"),
    SECURITY_TOKEN_SERVICE("SECURITY_TOKEN_SERVICE"),
    MEDLEMSKAP_API_URL("MEDLEMSKAP_API_URL"),
}

class MissingEnvironmentVariable(message: String) : RuntimeException(message)

fun Environment.requireEnv(e: EnvironmentKey): String =
    this[e] ?: throw MissingEnvironmentVariable("""${e.key}, is not found in environment""")

operator fun Environment.get(e: EnvironmentKey): String? = this[e.key]