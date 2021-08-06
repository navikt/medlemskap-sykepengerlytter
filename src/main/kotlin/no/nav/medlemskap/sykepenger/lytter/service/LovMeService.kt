package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.config.Environment
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.domain.SykepengeSoknad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LovMeService(
    private val environment: Environment,
    //private val lovmeClient: LovMeClient<*> = LovMeClient(environment = environment)
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(LovMeService::class.java)
    }

    fun callLovMe(sykepengeSoknad: SykepengeSoknad)
    {

    //Todo : Implement
    }
    fun handle(soknadRecord: SoknadRecord)
    {
        soknadRecord.log()
    }
    private fun SoknadRecord.log() =
        LovMeService.log.info(
            "Received soknad  - id: ${sykepengeSoknad.id}, offsett: $offset, partiotion: $partition, topic: $topic"
        )
}
