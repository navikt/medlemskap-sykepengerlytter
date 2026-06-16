package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.brukersvar

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.FlexMessageRecord
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.Soknadstatus

open class BrukersvarHandler(
    private val persistenceService: PersistenceService,
    private val brukersvarMapper: BrukersvarMapper = BrukersvarMapper()
) {
    companion object {
        private val log = KotlinLogging.logger { }
    }

    /*
     * SP1220
     * */
    fun handleBrukerSporsmaal(flexMessageRecord: FlexMessageRecord) {
        val brukersporsmaal: Brukersporsmaal = brukersvarMapper.mapMessage(flexMessageRecord)

        if (!brukersporsmaal.erSendt()) {
            loggFiltrertFeilStatus(flexMessageRecord, brukersporsmaal)
            return
        }

        if (brukersporsmaalErLagretFraFør(brukersporsmaal)) {
            loggFiltrertDuplikat(flexMessageRecord, brukersporsmaal)
            return
        }

        lagreBrukersporsmaal(flexMessageRecord, brukersporsmaal)
    }

    private fun Brukersporsmaal.erSendt(): Boolean =
        status == Soknadstatus.SENDT.toString()

    private fun brukersporsmaalErLagretFraFør(brukersporsmaal: Brukersporsmaal): Boolean =
        persistenceService.hentbrukersporsmaalForSoknadID(brukersporsmaal.soknadid) != null

    private fun lagreBrukersporsmaal(
        flexMessageRecord: FlexMessageRecord,
        brukersporsmaal: Brukersporsmaal
    ) {
        persistenceService.lagreBrukersporsmaal(brukersporsmaal)
        log.info(
            "Brukerspørsmål for søknad ${flexMessageRecord.key} lagret til databasen",
            kv("callId", flexMessageRecord.key),
            kv("dato", brukersporsmaal.eventDate),
            kv("partition", flexMessageRecord.partition),
        )
    }

    private fun loggFiltrertDuplikat(
        flexMessageRecord: FlexMessageRecord,
        brukersporsmaal: Brukersporsmaal
    ) {
        log.info(
            "Flex melding for søknad ${flexMessageRecord.key}, " +
                    "offset : ${flexMessageRecord.offset}, " +
                    "partition : ${flexMessageRecord.partition}," +
                    "filtrert ut. duplikat melding: ${brukersporsmaal.soknadid}"
        )
    }

    private fun loggFiltrertFeilStatus(
        flexMessageRecord: FlexMessageRecord,
        brukersporsmaal: Brukersporsmaal
    ) {
        log.info(
            "Flex melding for søknad ${flexMessageRecord.key}, " +
                    "offset : ${flexMessageRecord.offset}, " +
                    "partition : ${flexMessageRecord.partition}," +
                    "filtrert ut. Feil status : ${brukersporsmaal.status}"
        )
    }
}
