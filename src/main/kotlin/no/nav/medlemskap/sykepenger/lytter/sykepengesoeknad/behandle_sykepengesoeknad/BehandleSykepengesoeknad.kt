package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadRecord

open class BehandleSykepengesoeknad(
    private val sykepengesoeknadVurdering: SykepengesoeknadVurdering
) {

    suspend fun behandleSykepengesøknad(sykepengesøknadRecord: SykepengesoeknadRecord) {
        val sykepengesøknad = JacksonParser().parse(sykepengesøknadRecord.value)

        val soknadRecord = SoknadRecord(
            sykepengesøknadRecord.partition,
            sykepengesøknadRecord.offset,
            sykepengesøknadRecord.value,
            sykepengesøknadRecord.key,
            sykepengesøknadRecord.topic,
            sykepengesøknad
        )
        sykepengesoeknadVurdering.vurder(soknadRecord)
    }
}
