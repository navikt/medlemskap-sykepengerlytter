package no.nav.persistence

import no.nav.medlemskap.saga.persistence.Brukersporsmaal
import no.nav.medlemskap.saga.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.persistence.BrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapVurdertRepository

class MedlemskapVurdertInMemmoryRepository: MedlemskapVurdertRepository {
    var storage = mutableListOf<VurderingDao>()
    override fun finnVurdering(fnr: String): List<VurderingDao> {
        return  storage.filter { it.fnr == fnr }
    }

    override fun lagreVurdering(vurderingDao: VurderingDao) {
        storage.add(vurderingDao)
    }

}
class BrukersporsmaalInMemmoryRepository: BrukersporsmaalRepository {
    var storage = mutableListOf<Brukersporsmaal>()
    override fun finnBrukersporsmaal(fnr: String): List<Brukersporsmaal> {
        return  storage.filter { it.fnr == fnr }
    }

    override fun lagreBrukersporsmaal(brukersporsmaal: Brukersporsmaal) {
        storage.add(brukersporsmaal)
    }



}