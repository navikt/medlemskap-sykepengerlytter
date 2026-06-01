package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import org.slf4j.MarkerFactory

class Respons {
    private val logger = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun lagFlexRespons(medlemskapOppslagResponse: String, medlemskapOppslagRequest: MedlOppslagRequest, callId: String): FlexRespons {
        val foreløpigResponse = RegelMotorResponsHandler().utledResultat(medlemskapOppslagResponse)
        val forrigeBrukerspørsmål = MedlemskapOppslagService(Configuration()).finnForrigeBrukerspørsmål(medlemskapOppslagRequest)
        val flexRespons = opprettResponsTilFlex(foreløpigResponse, forrigeBrukerspørsmål, callId)
        if (flexRespons.sporsmal.contains(Spørsmål.OPPHOLDSTILATELSE)){
            flexRespons.kjentOppholdstillatelse = RegelMotorResponsHandler().hentOppholdstillatelsePeriode(medlemskapOppslagResponse)
        }
        logger.info(
            teamLogs,
            "Svarer brukerspørsmål",
            kv("callId", callId),
            kv("fnr", medlemskapOppslagRequest.fnr),
            kv("brukersporsmal", JacksonParser().ToJson(flexRespons.sporsmal).toPrettyString()),
            kv("endpoint", "brukersporsmal"),
            kv("eksiterende_sporsmaal",JacksonParser().ToJson(forrigeBrukerspørsmål).toPrettyString())
        )
        return flexRespons
    }
}