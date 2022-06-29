package no.nav.medlemskap.sykepenger.lytter.config

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.medlemskap.saga.persistence.*
import no.nav.medlemskap.sykepenger.lytter.FnrResponse
import no.nav.medlemskap.sykepenger.lytter.aarsaker
import java.util.*

val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .registerModule(JavaTimeModule())
    .configure(SerializationFeature.INDENT_OUTPUT, true)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)

fun mapToFnrResponse(dao: SykepengerVurderingDao) : FnrResponse {
    val jsonNode = objectMapper.readTree(dao.json)
    val resultat = jsonNode.get("resultat")
    val aarsaker:ArrayNode = resultat.withArray("årsaker")
    if (aarsaker.isEmpty){
        return FnrResponse(dao.id,dao.soknadId,dao.date,resultat.get("svar").asText(),null)
    }

    return FnrResponse(dao.id,dao.soknadId,dao.date,resultat.get("svar").asText(),aarsaker.map { aarsaker(it.get("regelId").asText(),it.get("begrunnelse").asText()) })
}
fun filterVurderinger(vurderinger:List<SykepengerVurderingDao>, periode: Periode, fnr:String) : Optional<SykepengerVurderingDao> {
    val vurdering = vurderinger
        .filter { it.fnr()==fnr }
        .sortedBy { it.periode().fom }
        .filter { periode.begynnerIPerioden(it.periode()) }
        .stream().findFirst()
    return vurdering
}