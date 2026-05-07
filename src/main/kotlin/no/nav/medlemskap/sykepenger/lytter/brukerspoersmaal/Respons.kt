package no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal

import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.medlemskap.sykepenger.lytter.clients.medloppslag.MedlOppslagRequest
import no.nav.medlemskap.sykepenger.lytter.jackson.JacksonParser
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRespons
import no.nav.medlemskap.sykepenger.lytter.rest.Spørsmål
import no.nav.medlemskap.sykepenger.lytter.service.BomloService
import no.nav.medlemskap.sykepenger.lytter.service.RegelMotorResponsHandler
import no.nav.medlemskap.sykepenger.lytter.service.opprettResponsTilFlex
import org.slf4j.MarkerFactory

class Respons {
    private val logger = KotlinLogging.logger { }
    private val teamLogs = MarkerFactory.getMarker("TEAM_LOGS")

    fun lagRespons(lovmeresponse: String, lovmeRequest: MedlOppslagRequest, bomloService: BomloService, callId: String): FlexRespons {
        val foreløpigResponse = RegelMotorResponsHandler().utledResultat(lovmeresponse)
        val forrigeBrukerspørsmål = bomloService.finnForrigeBrukerspørsmål(lovmeRequest)
        val flexRespons = opprettResponsTilFlex(foreløpigResponse, forrigeBrukerspørsmål, callId)
        if (flexRespons.sporsmal.contains(Spørsmål.OPPHOLDSTILATELSE)){
            flexRespons.kjentOppholdstillatelse = RegelMotorResponsHandler().hentOppholdstillatelsePeriode(lovmeresponse)
        }
        logger.info(
            teamLogs,
            "Svarer brukerspørsmål",
            kv("callId", callId),
            kv("fnr", lovmeRequest.fnr),
            kv("brukersporsmal", JacksonParser().ToJson(flexRespons.sporsmal).toPrettyString()),
            kv("endpoint", "brukersporsmal"),
            kv("eksiterende_sporsmaal",JacksonParser().ToJson(forrigeBrukerspørsmål).toPrettyString())
        )
        return flexRespons
    }
}