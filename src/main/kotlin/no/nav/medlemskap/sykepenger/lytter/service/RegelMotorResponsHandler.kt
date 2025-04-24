package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode

import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Periode
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.rest.Svar
import java.time.LocalDate

val REGLER_DET_SKAL_LAGES_BRUKERSPØRSMÅL_FOR:List<String> = listOf("REGEL_3","REGEL_19_3_1", "REGEL_15","REGEL_C","REGEL_12", "REGEL_20", "REGEL_34", "REGEL_21", "REGEL_25", "REGEL_5")
val MULTI_REGLER_DET_SKAL_LAGES_BRUKERSPØRSMÅL_FOR:List<String> = listOf("REGEL_11")
val MEDL_REGLER:List<String> = listOf("REGEL_1_3_1","REGEL_1_3_3","REGEL_1_3_4","REGEL_1_3_5")



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
    fun hentOppholdsTilatelsePeriode(lovmeresponse: String) : Periode? {
        val lovmeresponseNode = objectMapper.readTree(lovmeresponse)
        val periode = lovmeresponseNode.oppholdsTillatelsePeriode()
        if (periode != null){
            val fom:LocalDate = LocalDate.parse(periode.get("fom").asText())
            val tom:LocalDate? = runCatching {
                LocalDate.parse(periode.get("tom").asText())}
                .getOrNull()
            return Periode(fom,tom)
        }
        return null
    }

    fun enkeltReglerDetIkkeSkalLagesBrukerSporsmaalPaa(lovmeresponseNode: JsonNode): Boolean{
        return !lovmeresponseNode.aarsakerInneholderKunEnReglel(REGLER_DET_SKAL_LAGES_BRUKERSPØRSMÅL_FOR) &&
                !lovmeresponseNode.aarsakerInneholderKunEnReglelSomStarterMed(MULTI_REGLER_DET_SKAL_LAGES_BRUKERSPØRSMÅL_FOR) &&
                !lovmeresponseNode.aarsakerInneholderMEDLRegler(MEDL_REGLER)
    }
    fun multiReglerDetIkkeSkalLagesBrukerSporsmaalPaa(lovmeresponseNode: JsonNode): Boolean{
        return !lovmeresponseNode.alleAarsakerErILista(REGLER_DET_SKAL_LAGES_BRUKERSPØRSMÅL_FOR,MULTI_REGLER_DET_SKAL_LAGES_BRUKERSPØRSMÅL_FOR)
    }

    private fun createFlexRespons(lovmeresponseNode: JsonNode) :FlexRespons{

        //Lag bruker spørsmål kun for de reglene som er avklart
        if (enkeltReglerDetIkkeSkalLagesBrukerSporsmaalPaa(lovmeresponseNode)  &&  multiReglerDetIkkeSkalLagesBrukerSporsmaalPaa(lovmeresponseNode)){
            return FlexRespons(Svar.UAVKLART, emptySet())
        }
        if (lovmeresponseNode.erEosBorger()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE))
        }
        if (lovmeresponseNode.erTredjelandsborgerMedEØSFamilie() && lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE))
        }
        if (lovmeresponseNode.erTredjelandsborgerMedEØSFamilie() && !lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_EØS_OMRÅDE))
        }
        if (lovmeresponseNode.erTredjelandsborger() && !lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.OPPHOLDSTILATELSE,Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE))
        }
        if (lovmeresponseNode.erTredjelandsborger() && lovmeresponseNode.harOppholdsTilatelse()){
            return FlexRespons(Svar.UAVKLART, setOf(Spørsmål.ARBEID_UTENFOR_NORGE,Spørsmål.OPPHOLD_UTENFOR_NORGE))
        }
         throw IllegalStateException()
    }
}
fun JsonNode.erEosBorger():Boolean{
    return this.finnSvarPaaRegel("REGEL_2")
}


fun JsonNode.finnSvarPaaRegelFlyt(regelID:String):Boolean{
    try{

        val svar = this.get("resultat").get("delresultat")
            .filter { it.get("regelId").asText().equals(regelID) }.first().get("svar").asText()

        if (svar.equals("JA")){
            return true
        }
        return false
    }
    catch (e:Exception){
        return false
    }
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

fun JsonNode.aarsakerInneholderKunEnReglelSomStarterMed(regler:List<String>):Boolean{
    val found = regler.find {
        regel -> this.aarsaker().first().startsWith(regel)
    }
    return this.aarsaker().size == 1 && found != null
}

fun JsonNode.aarsakerInneholderMEDLRegler(regler:List<String>):Boolean{
    return this.aarsaker().any { it in regler }
}

/**
 * Sjekker om alle årsaker enten er i listen over enkeltregler eller starter med en av multireglene.
 *
 * @param listeAvEnkeltRegler Liste over regler som kan aksepteres direkte.
 * @param listeAvmultiregler Liste over regler som kan aksepteres hvis de starter med en av disse.
 * @return `true` hvis alle årsaker er i en av listene, ellers `false`.
 */
private fun JsonNode.alleAarsakerErILista(
    listeAvEnkeltRegler: List<String>,
    listeAvmultiregler: List<String>
): Boolean {
    val aarsaker = this.aarsaker() // Henter ut alle årsaker fra JSON-noden.

    // Hvis alle årsakene finnes i listen over enkeltregler, returner true umiddelbart.
    if (listeAvEnkeltRegler.containsAll(aarsaker)) {
        return true
    }

    // Filtrer ut årsaker som ikke finnes i listen over enkeltregler
    // og sjekk om alle gjenværende starter med en av multireglene.
    return aarsaker.filterNot { it in listeAvEnkeltRegler }
        .all { rest -> listeAvmultiregler.any { rest.startsWith(it) } }
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
fun JsonNode.harOppholdsTilatelse():Boolean {


    if (finnSvarPaaRegelFlyt("REGEL_OPPHOLDSTILLATELSE")){
        return true
    }

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

/*
* ment å brukes ved uthenting av perioden for oppholdstilatelser der bruker har gått ut på brudd på regel 19_3.
* Usikkert om denne vil fungere dersom det ikke er oppholdstillatelse Pa Samme Vilkar
* */
fun JsonNode.oppholdsTillatelsePeriode():JsonNode? {
     runCatching { this.get("datagrunnlag").get("oppholdstillatelse").get("gjeldendeOppholdsstatus").get("oppholdstillatelsePaSammeVilkar").get("periode") }
        .onSuccess {  return it }
        .onFailure {  return null }
    return null
}