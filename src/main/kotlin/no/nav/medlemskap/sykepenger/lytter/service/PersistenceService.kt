package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.nav.medlemskap.saga.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.domain.MedlemskapVurdertRecord
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapVurdertRepository
import java.time.LocalDate

class PersistenceService(
    private val medlemskapVurdertRepository: MedlemskapVurdertRepository
)
{
    companion object {
        private val log = KotlinLogging.logger { }

    }

    suspend fun handle(vurdertRecord: MedlemskapVurdertRecord){
        try {
            medlemskapVurdertRepository.lagreVurdering(VurderingDaoMapper().mapJsonNodeToVurderingDao(vurdertRecord.key!!,vurdertRecord.medlemskapVurdert))
            log.info { "vurdering av sykepengesøknad med id ${vurdertRecord.key} lagret i databasen" }
        }
        catch (throwable :Throwable){
            log.error { "vurdering av sykepengesøknad med id ${vurdertRecord.key} ble ikke lagret lagret i databasen. Error : ${throwable.cause}" }
        }

    }


}
class VurderingDaoMapper(){
    fun mapJsonNodeToVurderingDao(id:String,jsonNode: JsonNode):VurderingDao{
        val fnr = jsonNode.get("datagrunnlag").get("fnr").asText()
        val fom = jsonNode.get("datagrunnlag").get("periode").get("fom").asText()
        val tom = jsonNode.get("datagrunnlag").get("periode").get("tom").asText()
        val status = jsonNode.get("resultat").get("svar").asText()
        return VurderingDao(id,fnr, LocalDate.parse(fom), LocalDate.parse(tom),status)
    }
}