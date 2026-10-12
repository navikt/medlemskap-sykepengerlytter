package no.nav.medlemskap.sykepenger.lytter.persistence

import kotliquery.Row
import javax.sql.DataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using

import no.nav.medlemskap.sykepenger.lytter.security.sha256

interface KlassifiseringRepositoryInterface {
    fun finnKlassifisering(fnr: String): List<KlassifiseringDao>
    fun lagreKlassifisering(klassifiseringDao: KlassifiseringDao)
    fun slettKlassifisering(fnr: String): Int
}

class KlassifiseringRepository(val dataSource: DataSource) : KlassifiseringRepositoryInterface {
    val LAGRE_KLASSIFISERING = "INSERT INTO syk_vurdering(id,fnr, fom,tom,status) VALUES(?, ?, ?, ?, ?)"
    val FIND_BY_FNR = "select * from syk_vurdering where fnr = ?"
    val SLETT_FOR_FNR = "DELETE FROM syk_vurdering WHERE fnr = ?"

    override fun finnKlassifisering(fnr: String): List<KlassifiseringDao> {
        return using(sessionOf(dataSource)) {
                it.run(queryOf(FIND_BY_FNR, fnr.sha256()).map(tilKlassifiseringDao).asList)
        }
    }

    override fun lagreKlassifisering(klassifiseringDao: KlassifiseringDao) {
        using(sessionOf(dataSource)) { session ->
            session.transaction {
                it.run(queryOf(LAGRE_KLASSIFISERING, klassifiseringDao.id,klassifiseringDao.fnr.sha256(), klassifiseringDao.fom,klassifiseringDao.tom, klassifiseringDao.status).asExecute)
            }

        }
    }

    //TODO: Brukes til testrammeverk, burde flyttes til egen testrameverk struktur
    override fun slettKlassifisering(fnr: String): Int {
        return using(sessionOf(dataSource)) { session ->
            session.transaction {
                it.run(queryOf(SLETT_FOR_FNR, fnr.sha256()).asUpdate)
            }
        }
    }

    val tilKlassifiseringDao: (Row) -> KlassifiseringDao = { row ->
        KlassifiseringDao(
            row.string("id"),
            row.string("fnr"),
            row.localDate("fom"),
            row.localDate("tom"),
            row.string("status")
        )
    }
}
