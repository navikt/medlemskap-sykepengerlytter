package no.nav.medlemskap.sykepenger.lytter.persistence

import kotliquery.Row
import javax.sql.DataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.medlemskap.saga.persistence.Brukersporsmaal
import no.nav.medlemskap.saga.persistence.FlexBrukerSporsmaal
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.security.sha256

interface BrukersporsmaalRepository {
    fun finnBrukersporsmaal(fnr: String): List<Brukersporsmaal>
    fun lagreBrukersporsmaal(brukersporsmaal: Brukersporsmaal)
}

class PostgresBrukersporsmaalRepository(val dataSource: DataSource) : BrukersporsmaalRepository {
    val INSERT_BRUKER_SPORSMAAL = "INSERT INTO brukersporsmaal(fnr,soknadid, eventDate,ytelse,status,sporsmaal) VALUES(?, ?, ?, ?, ?, to_json(?::json))"
    val FIND_BY_FNR = "select * from brukersporsmaal where fnr = ?"

    override fun finnBrukersporsmaal(fnr: String): List<Brukersporsmaal> {


        return using(sessionOf(dataSource)) {
                it.run(queryOf(FIND_BY_FNR, fnr.sha256()).map(toBrukersporsmaalDao).asList)
        }

    }

    override fun lagreBrukersporsmaal(brukersporsmaal: Brukersporsmaal) {
        using(sessionOf(dataSource)) { session ->
            session.transaction {
                it.run(queryOf(INSERT_BRUKER_SPORSMAAL, brukersporsmaal.fnr.sha256(),brukersporsmaal.soknadid, brukersporsmaal.eventDate,brukersporsmaal.ytelse, brukersporsmaal.status,
                    brukersporsmaal.sporsmaal?.let { it1 -> JacksonParser().parse(it1) }).asExecute)
            }

        }
    }


    fun using(datasource: DataSource): PostgresBrukersporsmaalRepository {
        return PostgresBrukersporsmaalRepository(datasource)
    }

    val toBrukersporsmaalDao: (Row) -> Brukersporsmaal = { row ->
        Brukersporsmaal(
            fnr=row.string("fnr"),
            soknadid = row.string("soknadid").toString(),
            eventDate= row.localDate("eventDate"),
            ytelse= row.string("ytelse"),
            status= row.string("status"),
            sporsmaal= JacksonParser().parseFlexBrukerSporsmaal(row.string("sporsmaal"))

        )
    }
}
