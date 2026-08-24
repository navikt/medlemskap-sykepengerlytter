package no.nav.persistence

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.VurderingDao
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

    override fun slettVurderingsstatus(fnr: String): Int {
        val count = storage.count { it.fnr == fnr }
        storage.removeAll { it.fnr == fnr }
        return count
    }

}
class BrukersporsmaalInMemmoryRepository: BrukersporsmaalRepository {
    var storage = mutableListOf<Brukerspørsmål>()
    override fun finnBrukersporsmaal(fnr: String): List<Brukerspørsmål> {
        return  storage.filter { it.fnr == fnr }
    }

    override fun lagreBrukersporsmaal(brukerspørsmål: Brukerspørsmål) {
        storage.add(brukerspørsmål)
    }

    override fun finnBrukersporsmaalForSoknad(id: String): Brukerspørsmål? {
        return storage.filter { it.soknadid == id }.firstOrNull()
    }

    override fun slettBrukersporsmaal(fnr: String): Int {
        val count = storage.count { it.fnr == fnr }
        storage.removeAll { it.fnr == fnr }
        return count
    }


}