package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import org.slf4j.MarkerFactory

class Respons(
    private val brukersporsmaalService: BrukersporsmaalService = BrukersporsmaalService(),
    private val medlemskapVurderingMapper: MedlemskapVurderingMapper = MedlemskapVurderingMapper(),
    private val regelMotorResponsHandler: RegelMotorResponsHandler = RegelMotorResponsHandler()
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

        return regelMotorResponsHandler
            .tilForeslåttFlexRespons(medlemskapVurdering)
            .medSpørsmålSomSkalStilles(gjenbrukbareSpørsmål)
            .medKjentOppholdstillatelseFra(medlemskapVurdering)
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