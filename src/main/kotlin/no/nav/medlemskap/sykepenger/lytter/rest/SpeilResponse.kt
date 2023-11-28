package no.nav.medlemskap.sykepenger.lytter.rest

import com.fasterxml.jackson.databind.JsonNode
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.Brukerinput
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.inneholderNyModell
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser

data class SpeilResponse(val vurderingsID:String,val fnr:String,val speilSvar: Speilsvar)

enum class Speilsvar{
    JA,
    NEI,
    UAVKLART,
    UAVKLART_MED_BRUKERSPORSMAAL
}

fun JsonNode.lagSpeilRespons(default:String):SpeilResponse{
    val fnr = this.get("datagrunnlag").get("fnr").asText()
    val vurderingsID = this.finnSoknadID(default)
    return SpeilResponse(vurderingsID,fnr,this.finnSpeilSvar())
}

fun JsonNode.finnSpeilSvar():Speilsvar{
    val brukerinput:Brukerinput = JacksonParser().toDomainObject(this.get("datagrunnlag").get("brukerinput"))

    if (this.finnSvar().equals("JA")){
        return Speilsvar.JA
    }
    else if (this.finnSvar().equals("NEI")){
        return Speilsvar.NEI
    }
    else{
        if (brukerinput.inneholderNyModell()){
            return Speilsvar.UAVKLART_MED_BRUKERSPORSMAAL
        }
        return Speilsvar.UAVKLART
    }
}

fun JsonNode.finnSvar():String{
     runCatching { this.get("konklusjon").get(0).get("status").asText() }
        .onSuccess { return  it }
        .onFailure { return this.get("resultat").get("svar").asText() }
    return ""

}
fun JsonNode.finnSoknadID(callid:String):String{
    runCatching { this.get("vurderingsID").asText() }
        .onSuccess { return  it }
        .onFailure { return callid }
    return callid
}