package no.nav.medlemskap.sykepenger.lytter

import com.zaxxer.hikari.HikariDataSource
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.HentGjenbrukbareBrukerspoersmaal
import no.nav.medlemskap.sykepenger.lytter.brukerspoersmaal.LagFlexRespons
import no.nav.medlemskap.sykepenger.lytter.clients.RestClients
import no.nav.medlemskap.sykepenger.lytter.clients.azuread.AzureAdClient
import no.nav.medlemskap.sykepenger.lytter.config.Configuration
import no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus.FinnMedlemskapsstatus
import no.nav.medlemskap.sykepenger.lytter.medlemskapsstatus.MedlemskapsstatusService
import no.nav.medlemskap.sykepenger.lytter.persistence.DataSourceBuilder
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresBrukersporsmaalRepository
import no.nav.medlemskap.sykepenger.lytter.persistence.PostgresMedlemskapVurdertRepository
import no.nav.medlemskap.sykepenger.lytter.security.AuthorizationHandler
import no.nav.medlemskap.sykepenger.lytter.service.GjenbrukBrukersvar
import no.nav.medlemskap.sykepenger.lytter.service.MedlemskapOppslagService
import no.nav.medlemskap.sykepenger.lytter.service.PersistenceService
import no.nav.medlemskap.sykepenger.lytter.service.TidligereBrukersvar
import no.nav.medlemskap.sykepenger.lytter.service.UtledBrukerinput
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.SpeilvurderingMapper
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.hent_vurdering.HentEllerOpprettVurdering
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.hent_vurdering.MedlemskapSagaService
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.opprett_vurdering.MedlemskapOppslagService as SpeilMedlemskapOppslagService
import no.nav.medlemskap.sykepenger.lytter.speilvurdering.opprett_vurdering.OpprettNyVurderingForSpeil
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.SykepengesoeknadMottak
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.BehandleSykepengesoeknad
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.LagreVurderingsstatus
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.behandle_sykepengesoeknad.SykepengesoeknadFiltrering
import no.nav.medlemskap.sykepenger.lytter.sykepengesoeknad.lagre_brukerspoersmaal.LagreBrukerspoersmaal
import no.nav.medlemskap.sykepenger.lytter.nais.TestrammeverkService

class ApplicationComponents private constructor(
    val configuration: Configuration,
    val dataSource: HikariDataSource,
    val persistenceService: PersistenceService,
    val authorizationHandler: AuthorizationHandler,
    val medlemskapOppslagService: MedlemskapOppslagService,
    val lagFlexRespons: LagFlexRespons,
    val speilvurderingMapper: SpeilvurderingMapper,
    val hentEllerOpprettVurdering: HentEllerOpprettVurdering,
    val finnMedlemskapsstatus: FinnMedlemskapsstatus,
    val sykepengesøknadMottak: SykepengesoeknadMottak,
    val testrammeverkService: TestrammeverkService
) {
    companion object {
        fun create(env: Map<String, String>): ApplicationComponents {
            val configuration = Configuration()
            val dataSource = DataSourceBuilder(env).getDataSource()
            val persistenceService = PersistenceService(
                PostgresMedlemskapVurdertRepository(dataSource),
                PostgresBrukersporsmaalRepository(dataSource)
            )
            val restClients = RestClients(AzureAdClient(configuration))
            val medlOppslagClient = restClients.medlOppslag(configuration.register.medlemskapOppslagBaseUrl)
            val sagaClient = restClients.saga(configuration.register.medlemskapSagaBaseUrl)
            val medlemskapOppslagService = MedlemskapOppslagService(medlOppslagClient)
            val tidligereBrukersvar = TidligereBrukersvar(persistenceService)
            val gjenbrukBrukersvar = GjenbrukBrukersvar(tidligereBrukersvar)
            val lagFlexRespons = LagFlexRespons(HentGjenbrukbareBrukerspoersmaal(tidligereBrukersvar))
            val speilvurderingMapper = SpeilvurderingMapper()
            val opprettNyVurderingForSpeil = OpprettNyVurderingForSpeil(
                medlemskapOppslagService = SpeilMedlemskapOppslagService(medlOppslagClient),
                utledBrukerinput = UtledBrukerinput(gjenbrukBrukersvar)
            )
            val hentEllerOpprettVurdering = HentEllerOpprettVurdering(
                medlemskapSagaService = MedlemskapSagaService(sagaClient),
                opprettNyVurderingForSpeil = opprettNyVurderingForSpeil,
                speilvurderingMapper = speilvurderingMapper
            )
            val sykepengesøknadMottak = SykepengesoeknadMottak(
                behandleSykepengesøknad = BehandleSykepengesoeknad(
                    filtrering = SykepengesoeknadFiltrering(persistenceService),
                    utledBrukerinput = UtledBrukerinput(gjenbrukBrukersvar),
                    lagreVurderingsstatus = LagreVurderingsstatus(persistenceService),
                    medlemskapOppslagService = medlemskapOppslagService
                ),
                lagreBrukerspoersmaal = LagreBrukerspoersmaal(persistenceService)
            )

            return ApplicationComponents(
                configuration = configuration,
                dataSource = dataSource,
                persistenceService = persistenceService,
                authorizationHandler = AuthorizationHandler(),
                medlemskapOppslagService = medlemskapOppslagService,
                lagFlexRespons = lagFlexRespons,
                speilvurderingMapper = speilvurderingMapper,
                hentEllerOpprettVurdering = hentEllerOpprettVurdering,
                finnMedlemskapsstatus = FinnMedlemskapsstatus(
                    persistenceService,
                    MedlemskapsstatusService(sagaClient)
                ),
                sykepengesøknadMottak = sykepengesøknadMottak,
                testrammeverkService = TestrammeverkService(persistenceService)
            )
        }
    }
}
