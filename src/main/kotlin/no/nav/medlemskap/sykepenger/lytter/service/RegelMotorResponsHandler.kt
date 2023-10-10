package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode

import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar

val REGLER_DET_SKAL_LAGES_BRUKERSPØRSMÅL_FOR:List<String> = listOf("REGEL_C")

class RegelMotorResponsHandler {

    fun interpretLovmeRespons(lovmeresponse: String) : FlexRespons {
        val lovmeresponseNode = objectMapper.readTree(lovmeresponse)
        when(lovmeresponseNode.svar()){
            "UAVKLART"->{return createFlexRespons(lovmeresponseNode)}
            "JA"->{return FlexRespons(svar = Svar.JA, emptySet())}
            "NEI"->{return FlexRespons(svar = Svar.NEI, emptySet())}
            else -> {throw IllegalStateException()}
        }




    }

    private fun createFlexRespons(lovmeresponseNode: JsonNode?) :FlexRespons{

        //Lag bruker spørsmål kun for de reglene som er avklart
        if (!lovmeresponseNode!!.aarsakerInneholderKunEnReglel(REGLER_DET_SKAL_LAGES_BRUKERSPØRSMÅL_FOR)){
            return FlexRespons(Svar.UAVKLART, emptySet())
        }
        if (lovmeresponseNode!!.erEosBorger()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE))
        }
        if (lovmeresponseNode!!.erTredjelandsborgerMedEØSFamilie() && lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE))
        }
        if (lovmeresponseNode!!.erTredjelandsborgerMedEØSFamilie() && !lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE))
        }
        if (lovmeresponseNode!!.erTredjelandsborger() && !lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE))
        }
        if (lovmeresponseNode!!.erTredjelandsborger() && lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE))
        }
         throw IllegalStateException()
    }
}
fun JsonNode.erEosBorger():Boolean{
    return this.finnSvarPaaRegel("REGEL_2")
}

fun JsonNode.finnSvarPaaRegel(regelID:String):Boolean{
    val regel = this.alleRegelResultat().finnRegel(regelID)
    if (regel!=null){
        return regel.get("svar").asText()=="JA"
    }
    return false
}
fun JsonNode.alleRegelResultat():List<JsonNode>{
    return this.get("resultat").get("delresultat").flatMap { it.get("delresultat")}
}
fun JsonNode.aarsaker():List<String>{
    return this.get("resultat").get("årsaker").map { it.get("regelId").asText()}
}
fun JsonNode.aarsakerInneholderEnEllerFlereRegler(regler:List<String>):Boolean{
    return this.aarsaker().any { it in regler }
}
fun JsonNode.aarsakerInneholderKunEnReglel(regler:List<String>):Boolean{
    return this.aarsaker().size == 1 && this.aarsaker().any { it in regler }
}

fun List<JsonNode>.finnRegel(regelID:String):JsonNode?{
    return this.find { it.get("regelId").asText()==regelID }
}
fun JsonNode.erTredjelandsborgerMedEØSFamilie():Boolean{
    return finnSvarPaaRegel("REGEL_28") && finnSvarPaaRegel("REGEL_29")
}
fun JsonNode.erTredjelandsborger():Boolean{
    return !this.finnSvarPaaRegel("REGEL_2")
}
fun JsonNode.erBritiskBorger():Boolean{
    return this.finnSvarPaaRegel("REGEL_19_7")
}
fun JsonNode.harOppholdsTilatelse():Boolean{
    /*
    * Sjekk uavklart svar fra UDI
    * */
    if (this.finnSvarPaaRegel("REGEL_19_1")){
        return false
    }
    /*
    * Sjekk Oppholdstilatelse tilbake i tid
    * */
    if (!this.finnSvarPaaRegel("REGEL_19_3")){
        return false
    }
    /*
    * Sjekk oppholdstilatelsen i  arbeidsperioden
    * */
    if (!this.finnSvarPaaRegel("REGEL_19_3_1")){
        return false
    }
    /*
     *Har bruker opphold på samme vilkår flagg?
     */
    if (this.finnSvarPaaRegel("REGEL_19_8")){
        return false

    }
    return true
}
fun JsonNode.svar():String{
    return this.get("resultat").get("svar").asText()
}