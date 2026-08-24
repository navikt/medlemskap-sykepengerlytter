package no.nav.medlemskap.sykepenger.lytter.persistence

import kotliquery.Row
import javax.sql.DataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using

import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.security.sha256

interface BrukersporsmaalRepository {
    fun finnBrukersporsmaal(fnr: String): List<Brukerspørsmål>
    fun lagreBrukersporsmaal(brukerspørsmål: Brukerspørsmål)
    fun finnBrukersporsmaalForSoknad(id: String) : Brukerspørsmål?
    fun slettBrukersporsmaal(fnr: String): Int
}

class PostgresBrukersporsmaalRepository(val dataSource: DataSource) : BrukersporsmaalRepository {
    val INSERT_BRUKER_SPORSMAAL = "INSERT INTO brukersporsmaal(fnr,soknadid, eventDate,ytelse,status,sporsmaal) VALUES(?, ?, ?, ?, ?, to_json(?::json))"
    val FIND_BY_FNR = "select * from brukersporsmaal where fnr = ?"
    val FIND_BY_ID = "select * from brukersporsmaal where soknadid = ?"
    val DELETE_BY_FNR = "DELETE FROM brukersporsmaal WHERE fnr = ?"

    override fun finnBrukersporsmaal(fnr: String): List<Brukerspørsmål> {


        return using(sessionOf(dataSource)) {
                it.run(queryOf(FIND_BY_FNR, fnr.sha256()).map(toBrukerspørsmålDao).asList)
        }

    }

    override fun lagreBrukersporsmaal(brukerspørsmål: Brukerspørsmål) {

        val json = JacksonParser().ToJson(
            Brukerspørsmål(
            fnr = brukerspørsmål.fnr.sha256(),
            soknadid = brukerspørsmål.soknadid,
            eventDate = brukerspørsmål.eventDate,
            ytelse = brukerspørsmål.ytelse,
            status = brukerspørsmål.status,
            sporsmaal = brukerspørsmål.sporsmaal,
            oppholdstilatelse = brukerspørsmål.oppholdstilatelse,
            utfort_arbeid_utenfor_norge = brukerspørsmål.utfort_arbeid_utenfor_norge,
            oppholdUtenforNorge = brukerspørsmål.oppholdUtenforNorge,
            oppholdUtenforEOS = brukerspørsmål.oppholdUtenforEOS

        ))


        using(sessionOf(dataSource)) { session ->
            session.transaction {
                it.run(queryOf(INSERT_BRUKER_SPORSMAAL, brukerspørsmål.fnr.sha256(),brukerspørsmål.soknadid, brukerspørsmål.eventDate,brukerspørsmål.ytelse, brukerspørsmål.status,
                    brukerspørsmål.let { json.toPrettyString() }).asExecute)
            }

        }
    }

    override fun finnBrukersporsmaalForSoknad(id: String): Brukerspørsmål? {
            return using(sessionOf(dataSource)) {
                it.run(queryOf(FIND_BY_ID, id)
                    .map(toBrukerspørsmålDao)
                    .asList
                ).firstOrNull()
            }

        }

    override fun slettBrukersporsmaal(fnr: String): Int {
        return using(sessionOf(dataSource)) { session ->
            session.transaction {
                it.run(queryOf(DELETE_BY_FNR, fnr.sha256()).asUpdate)
            }
        }
    }
    }


    fun using(datasource: DataSource): PostgresBrukersporsmaalRepository {
        return PostgresBrukersporsmaalRepository(datasource)
    }

    val toBrukerspørsmålDao: (Row) -> Brukerspørsmål = { row ->


        try{
            val sporsmaal:Brukerspørsmål=  JacksonParser().toDomainObject(row.string("sporsmaal"))
             Brukerspørsmål(
                fnr=row.string("fnr"),
                soknadid = row.string("soknadid").toString(),
                eventDate= row.localDate("eventDate"),
                ytelse= row.string("ytelse"),
                status= row.string("status"),
                sporsmaal= sporsmaal.sporsmaal,
                oppholdstilatelse = sporsmaal.oppholdstilatelse,
                utfort_arbeid_utenfor_norge = sporsmaal.utfort_arbeid_utenfor_norge,
                oppholdUtenforNorge = sporsmaal.oppholdUtenforNorge,
                oppholdUtenforEOS = sporsmaal.oppholdUtenforEOS)

        }
        catch (e:Exception){
            val sporsmaal:ArbeidUtenforNorgeSpørsmål=  JacksonParser().toDomainObject(row.string("sporsmaal"))
            Brukerspørsmål(
                fnr=row.string("fnr"),
                soknadid = row.string("soknadid").toString(),
                eventDate= row.localDate("eventDate"),
                ytelse= row.string("ytelse"),
                status= row.string("status"),
                sporsmaal= sporsmaal)
        }


    }
