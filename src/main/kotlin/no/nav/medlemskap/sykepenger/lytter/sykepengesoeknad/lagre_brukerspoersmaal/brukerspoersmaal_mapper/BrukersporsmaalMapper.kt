package no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.brukerspoersmaal_mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.config.objectMapper
import no.nav.medlemskap.sykepenger.lytter.persistence.MedlemskapsBrukerSpørsmål
import org.slf4j.MarkerFactory

class BrukersporsmaalMapper(spørsmål: JsonNode, private val callId: String? = null) {
    val spørsmålListe: List<MedlemskapsBrukerSpørsmål> =
        objectMapper.convertValue<List<MedlemskapsBrukerSpørsmål>>(spørsmål)
            .filter { it.tag in medlemskapSpørsmålTags }

    val arbeidUtenforNorgeBrukerspørsmål =
        mapBrukerspørsmål("ARBEID_UTENFOR_NORGE") { mapArbeidUtenforNorgeBrukerspørsmål(spørsmålListe) }
    val oppholdstilatelseBrukerspørsmål =
        mapBrukerspørsmål("MEDLEMSKAP_OPPHOLDSTILLATELSE") { hentOppholdstillatelseBrukerspørsmål(spørsmålListe) }
    val utførtArbeidUtenforNorgeBrukerspørsmål =
        mapBrukerspørsmål("MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE") { hentUtførtArbeidUtenforNorgeBrukerSpørsmål(spørsmålListe) }
    val oppholdUtenforNorgeSpørsmål =
        mapBrukerspørsmål("MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE") { hentOppholdUtenforNorgeBrukerSpørsmål(spørsmålListe) }
    val oppholdUtenforEØSbrukerspørsmål =
        mapBrukerspørsmål("MEDLEMSKAP_OPPHOLD_UTENFOR_EOS") { hentOppholdUtenforEØSBrukerSpørsmål(spørsmålListe) }

    private fun <T> mapBrukerspørsmål(brukerspørsmål: String, mapper: () -> T?): T? {
        return try {
            mapper()
        } catch (e: Exception) {
            log.error(
                teamLogs,
                "Feil ved mapping av brukerspørsmål",
                kv("callId", callId),
                kv("brukersporsmal", brukerspørsmål),
                e
            )
            null
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
        private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

        private val medlemskapSpørsmålTags = setOf(
            "ARBEID_UTENFOR_NORGE",
            "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE",
            "MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE",
            "MEDLEMSKAP_OPPHOLD_UTENFOR_EOS",
            "MEDLEMSKAP_OPPHOLDSTILLATELSE_V2",
            "MEDLEMSKAP_OPPHOLDSTILLATELSE",
        )
    }
}