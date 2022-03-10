package no.nav.persistence

import no.nav.medlemskap.saga.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapVurdertRepository

class InMemmoryRepository: MedlemskapVurdertRepository {
    var storage = mutableListOf<VurderingDao>()
    override fun finnVurdering(fnr: String): List<VurderingDao> {
        return  storage.filter { it.fnr == fnr }
    }

    override fun lagreVurdering(vurderingDao: VurderingDao) {
        storage.add(vurderingDao)
    }

}