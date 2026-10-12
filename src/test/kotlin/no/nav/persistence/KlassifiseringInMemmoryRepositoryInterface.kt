package no.nav.persistence

import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.KlassifiseringDao
import no.nav.medlemskap.sykepenger.lytter.persistence.BrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.KlassifiseringRepositoryInterface

class KlassifiseringInMemmoryRepositoryInterface: KlassifiseringRepositoryInterface {
    var storage = mutableListOf<KlassifiseringDao>()
    override fun finnKlassifisering(fnr: String): List<KlassifiseringDao> {
        return  storage.filter { it.fnr == fnr }
    }

    override fun lagreKlassifisering(klassifiseringDao: KlassifiseringDao) {
        storage.add(klassifiseringDao)
    }

    override fun slettKlassifisering(fnr: String): Int {
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