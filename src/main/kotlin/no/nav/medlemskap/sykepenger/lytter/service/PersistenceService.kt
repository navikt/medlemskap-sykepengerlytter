package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.saga.persistence.VurderingDao
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
            vurdertRecord.logLagret()
        }
        catch (throwable :Throwable){
            vurdertRecord.logLagringFeilet(throwable)
        }

    }

    private fun MedlemskapVurdertRecord.logLagret() =
        PersistenceService.log.info(
            "Vurdering lagret til database - sykmeldingId: ${key}, offsett: $offset, partiotion: $partition, topic: $topic",
            StructuredArguments.kv("callId", key),
        )
    private fun MedlemskapVurdertRecord.logLagringFeilet(t:Throwable) =
        PersistenceService.log.error(
            "Vurdering ble ikke lagret til database - sykmeldingId: ${key}, offsett: $offset, partiotion: $partition, topic: $topic , reason : ${t.cause}",
            StructuredArguments.kv("callId", key),
        )


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