package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.domain.SoknadRecord

object SoknadRecordMapper {
    fun map(record: SykepengesoeknadRecord, soknad: LovmeSoknadDTO): SoknadRecord =
        SoknadRecord(
            partition = record.partition,
            offset = record.offset,
            value = record.value,
            key = record.key,
            topic = record.topic,
            sykepengeSoknad = soknad
        )
}
