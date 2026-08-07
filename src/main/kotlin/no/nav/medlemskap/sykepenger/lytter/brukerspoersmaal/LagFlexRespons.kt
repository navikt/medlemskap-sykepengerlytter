package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.generer_foreslaatte_brukerspoersmaal.ForeslaatteBrukerspoersmaalUtleder
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.flexrespons.tilFlexRespons
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.gjenbruk.finnSpørsmålSomSkalStilles
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import org.slf4j.MarkerFactory

class LagFlexRespons(
    private val brukersporsmaalService: BrukersporsmaalService = BrukersporsmaalService(),
    private val medlemskapVurderingMapper: MedlemskapVurderingMapper = MedlemskapVurderingMapper(),
    private val foreslaatteBrukerspoersmaalUtleder: ForeslaatteBrukerspoersmaalUtleder = ForeslaatteBrukerspoersmaalUtleder()
) {
    private val logger = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun lagFlexRespons(
        medlemskapOppslagResponse: String,
        medlemskapOppslagRequest: MedlOppslagRequest,
        callId: String
    ): FlexRespons {
        val medlemskapVurdering = medlemskapVurderingMapper.map(medlemskapOppslagResponse)
        val gjenbrukbareSpørsmål = brukersporsmaalService.finnForrigeBrukerspørsmål(medlemskapOppslagRequest)

        return foreslaatteBrukerspoersmaalUtleder
            .tilForeslåttBrukerspørsmål(medlemskapVurdering)
            .finnSpørsmålSomSkalStilles(gjenbrukbareSpørsmål)
            .tilFlexRespons(medlemskapVurdering)
            .also { loggRespons(it, gjenbrukbareSpørsmål, callId) }
    }

    private fun loggRespons(
        flexRespons: FlexRespons,
        gjenbrukbareSpørsmål: List<Spørsmål>,
        callId: String
    ) {
        logger.info(
            teamLogs,
            "Svarer brukerspørsmål",
            kv("callId", callId),
            kv("brukersporsmal", JacksonParser().ToJson(flexRespons.sporsmal).toPrettyString()),
            kv("endpoint", "brukersporsmal"),
            kv("eksisterende_sporsmaal", JacksonParser().ToJson(gjenbrukbareSpørsmål).toPrettyString())
        )
    }
}