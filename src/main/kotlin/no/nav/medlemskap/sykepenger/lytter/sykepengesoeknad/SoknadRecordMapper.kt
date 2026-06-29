package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad

import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.LovmeSoknadDTO
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SoknadRecord
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadRecord

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
