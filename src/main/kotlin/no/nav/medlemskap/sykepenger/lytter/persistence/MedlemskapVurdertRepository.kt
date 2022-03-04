package no.nav.medlemskap.sykepenger.lytter.persistence

import kotliquery.Row
import javax.sql.DataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.medlemskap.saga.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.security.sha256

interface MedlemskapVurdertRepository {
    fun finnVurdering(fnr: String): List<VurderingDao>
    fun lagreVurdering(vurderingDao: VurderingDao)
}

class PostgresMedlemskapVurdertRepository(val dataSource: DataSource) : MedlemskapVurdertRepository {
    val INSERT_VURDERING = "INSERT INTO syk_vurdering(id,fnr, fom,tom,status) VALUES(?, ?, ?, ?, ?)"
    val FIND_BY_FNR = "select * from syk_vurdering where fnr = ?"

    override fun finnVurdering(fnr: String): List<VurderingDao> {

        return using(sessionOf(dataSource)) {
                it.run(queryOf(FIND_BY_FNR, fnr.sha256()).map(toVurderingDao).asList)
        }
    }

    override fun lagreVurdering(vurderingDao: VurderingDao) {
        using(sessionOf(dataSource)) { session ->
            session.transaction {
                it.run(queryOf(INSERT_VURDERING, vurderingDao.id,vurderingDao.fnr.sha256(), vurderingDao.fom,vurderingDao.tom, vurderingDao.status).asExecute)
            }

        }
    }


    fun using(datasource: DataSource): PostgresMedlemskapVurdertRepository {
        return PostgresMedlemskapVurdertRepository(datasource)
    }

    val toVurderingDao: (Row) -> VurderingDao = { row ->
        VurderingDao(
            row.string("id"),
            row.string("fnr").toString(),
            row.localDate("fom"),
            row.localDate("tom"),
            row.string("status")
        )
    }
}
