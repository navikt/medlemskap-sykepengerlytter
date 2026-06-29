package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain

import java.time.LocalDateTime


enum class Kilde {
    KAFKA,
    LOVME_GCP
}

data class SykepengesoeknadRecord(
    val partition: Int,
    val offset: Long,
    val value: String,
    val key: String?,
    val topic: String,
    val timestamp: LocalDateTime,
    val timestampType: String,
    val kilde: Kilde = Kilde.KAFKA
)