package no.nav.medlemskap.sykepenger.lytter.security

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class Token(
    @param:JsonProperty(value = "access_token", required = true)
    val token: String,
    @param:JsonProperty(value = "token_type", required = true)
    val type: String,
    @param:JsonProperty(value = "expires_in", required = true)
    val expiresIn: Int
) {

    private val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expiresIn - 20L)

    fun hasExpired(): Boolean = expirationTime.isBefore(LocalDateTime.now())
}

fun Token?.shouldBeRenewed(): Boolean = this?.hasExpired() ?: true
