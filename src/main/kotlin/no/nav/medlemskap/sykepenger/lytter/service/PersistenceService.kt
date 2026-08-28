package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.sykepenger.lytter.domain.Status
import no.nav.medlemskap.sykepenger.lytter.domain.Vurderingsstatus
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukerspørsmål
import no.nav.medlemskap.sykepenger.lytter.persistence.BrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.SykepengesoeknadGrunnlag
import java.time.LocalDate

class PersistenceService(
    private val medlemskapVurdertRepository: MedlemskapVurdertRepository,
    private val brukersporsmaalRepository: BrukersporsmaalRepository
) {
    companion object {
        private val log = KotlinLogging.logger { }

    }

    fun lagreLovmeResponse(key:String,medlemskapVurdert:JsonNode) {
        try {
            medlemskapVurdertRepository.lagreVurdering(
                VurderingDaoMapper().mapJsonNodeToVurderingDao(
                    key,
                    medlemskapVurdert
                )
            )
            log.info(
                "Vurdering lagret til database - sykmeldingId: $key",
                StructuredArguments.kv("callId", key),
            )
        } catch (throwable: Exception) {
            log.error(
                "Vurdering ble ikke lagret til database - sykmeldingId: $key , reason : ${throwable.cause}",
                StructuredArguments.kv("callId", key),
            )
        }

    }

    fun hentbrukersporsmaalForSoknadID(soknadID:String):Brukerspørsmål?{
        return brukersporsmaalRepository.finnBrukersporsmaalForSoknad(soknadID)
    }
    fun hentbrukersporsmaalForFnr(fnr:String):List<Brukerspørsmål>{
        return brukersporsmaalRepository.finnBrukersporsmaal(fnr)
    }

    fun hentVurderingsstatus(fnr: String): List<Vurderingsstatus> {
        return medlemskapVurdertRepository.finnVurdering(fnr)
            .map { Vurderingsstatus(it.fnr, it.fom, it.tom, Status.valueOf(it.status)) }
    }

    fun lagrePaafolgendeSoknad(soknadDTO: SykepengesoeknadGrunnlag) {
        medlemskapVurdertRepository.lagreVurdering(
            VurderingDao(
                soknadDTO.id,
                soknadDTO.fnr,
                soknadDTO.fom!!,
                soknadDTO.tom!!,
                Status.PAFOLGENDE.toString()
            )
        )
    }
    fun lagreBrukersporsmaal(brukerspørsmål: Brukerspørsmål){
        brukersporsmaalRepository.lagreBrukersporsmaal(brukerspørsmål)
    }

    fun slettBrukersporsmaal(fnr: String): Int {
        return brukersporsmaalRepository.slettBrukersporsmaal(fnr)
    }

    fun slettVurderingsstatus(fnr: String): Int {
        return medlemskapVurdertRepository.slettVurderingsstatus(fnr)
    }
}

class VurderingDaoMapper {
    fun mapJsonNodeToVurderingDao(id: String, jsonNode: JsonNode): VurderingDao {
        val fnr = jsonNode.get("datagrunnlag").get("fnr").asText()
        val fom = jsonNode.get("datagrunnlag").get("periode").get("fom").asText()
        val tom = jsonNode.get("datagrunnlag").get("periode").get("tom").asText()
        val status = jsonNode.get("resultat").get("svar").asText()
        return VurderingDao(id, fnr, LocalDate.parse(fom), LocalDate.parse(tom), status)
    }
}