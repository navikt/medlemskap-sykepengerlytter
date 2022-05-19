package no.nav.medlemskap.sykepenger.lytter.service

import mu.KotlinLogging
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.FlexMessageRecord
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord

class FlexMessageHandler (
    private val configuration: Configuration,
    private val persistenceService: PersistenceService
) {
    companion object {
        private val log = KotlinLogging.logger { }

    }
    suspend fun handle(flexMessageRecord: FlexMessageRecord) {

    }
}