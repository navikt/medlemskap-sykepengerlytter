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
    fun finnBrukersporsmaalForSoknad(id: String) : Brukersporsmaal?
}

class PostgresBrukersporsmaalRepository(val dataSource: DataSource) : BrukersporsmaalRepository {
    val INSERT_BRUKER_SPORSMAAL = "INSERT INTO brukersporsmaal(fnr,soknadid, eventDate,ytelse,status,sporsmaal) VALUES(?, ?, ?, ?, ?, to_json(?::json))"
    val FIND_BY_FNR = "select * from brukersporsmaal where fnr = ?"
    val FIND_BY_ID = "select * from brukersporsmaal where soknadid = ?"

    override fun finnBrukersporsmaal(fnr: String): List<Brukersporsmaal> {


        return using(sessionOf(dataSource)) {
                it.run(queryOf(FIND_BY_FNR, fnr.sha256()).map(toBrukersporsmaalDao).asList)
        }

    }

    override fun lagreBrukersporsmaal(brukersporsmaal: Brukersporsmaal) {

        val json = JacksonParser().ToJson(
            Brukersporsmaal(
            fnr = brukersporsmaal.fnr.sha256(),
            soknadid = brukersporsmaal.soknadid,
            eventDate = brukersporsmaal.eventDate,
            ytelse = brukersporsmaal.ytelse,
            status = brukersporsmaal.status,
            sporsmaal = brukersporsmaal.sporsmaal,
            oppholdstilatelse = brukersporsmaal.oppholdstilatelse,
            utfort_arbeid_utenfor_norge = brukersporsmaal.utfort_arbeid_utenfor_norge
        ))


        using(sessionOf(dataSource)) { session ->
            session.transaction {
                it.run(queryOf(INSERT_BRUKER_SPORSMAAL, brukersporsmaal.fnr.sha256(),brukersporsmaal.soknadid, brukersporsmaal.eventDate,brukersporsmaal.ytelse, brukersporsmaal.status,
                    brukersporsmaal.let { json.toPrettyString() }).asExecute)
            }

        }
    }

    override fun finnBrukersporsmaalForSoknad(id: String): Brukersporsmaal? {
            return using(sessionOf(dataSource)) {
                it.run(queryOf(FIND_BY_ID, id)
                    .map(toBrukersporsmaalDao)
                    .asList
                ).firstOrNull()
            }

        }
    }


    fun using(datasource: DataSource): PostgresBrukersporsmaalRepository {
        return PostgresBrukersporsmaalRepository(datasource)
    }

    val toBrukersporsmaalDao: (Row) -> Brukersporsmaal = { row ->

        val sporsmaal:Brukersporsmaal=  JacksonParser().toDomainObject(row.string("sporsmaal"))

        Brukersporsmaal(
            fnr=row.string("fnr"),
            soknadid = row.string("soknadid").toString(),
            eventDate= row.localDate("eventDate"),
            ytelse= row.string("ytelse"),
            status= row.string("status"),
            sporsmaal= sporsmaal.sporsmaal,
            oppholdstilatelse = sporsmaal.oppholdstilatelse,
            utfort_arbeid_utenfor_norge = sporsmaal.utfort_arbeid_utenfor_norge)

    }
