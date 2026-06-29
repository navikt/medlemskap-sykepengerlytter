package no.nav.medlemskap.sykepenger.lytter.service

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.persistence.Brukersporsmaal
import no.nav.medlemskap.sykepenger.lytter.persistence.BrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.VurderingDao
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.domain.LovmeSoknadDTO
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
        } catch (throwable: Throwable) {
            log.error(
                "Vurdering ble ikke lagret til database - sykmeldingId: $key , reason : ${throwable.cause}",
                StructuredArguments.kv("callId", key),
            )
        }

    }

    fun hentbrukersporsmaalForSoknadID(soknadID:String):Brukersporsmaal?{
        return brukersporsmaalRepository.finnBrukersporsmaalForSoknad(soknadID)
    }
    fun hentbrukersporsmaalForFnr(fnr:String):List<Brukersporsmaal>{
        return brukersporsmaalRepository.finnBrukersporsmaal(fnr)
    }

    fun hentMedlemskap(fnr: String): List<Medlemskap> {
        return medlemskapVurdertRepository.finnVurdering(fnr)
            .map { Medlemskap(it.fnr, it.fom, it.tom, ErMedlem.valueOf(it.status)) }
    }

    fun lagrePaafolgendeSoknad(soknadDTO: LovmeSoknadDTO) {
        medlemskapVurdertRepository.lagreVurdering(
            VurderingDao(
                soknadDTO.id,
                soknadDTO.fnr,
                soknadDTO.fom!!,
                soknadDTO.tom!!,
                ErMedlem.PAFOLGENDE.toString()
            )
        )
    }
    fun lagreBrukersporsmaal(brukersporsmaal: Brukersporsmaal){
        brukersporsmaalRepository.lagreBrukersporsmaal(brukersporsmaal)
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