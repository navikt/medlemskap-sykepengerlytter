package no.nav.medlemskap.sykepenger.lytter.service

import no.nav.medlemskap.sykepenger.lytter.domain.ErMedlem
import no.nav.medlemskap.sykepenger.lytter.domain.Medlemskap
import no.nav.medlemskap.sykepenger.lytter.rest.FlexRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class bomloservicetest {
    @Test
    fun `identisk periode skal finnes`(){
        val medlemskap = listOf(
            Medlemskap("1", LocalDate.of(2022,1,1,), LocalDate.of(2022,1,31,), ErMedlem.JA),
            Medlemskap("1", LocalDate.of(2022,2,1,), LocalDate.of(2022,2,28,), ErMedlem.PAFOLGENDE),
            Medlemskap("1", LocalDate.of(2022,3,1,), LocalDate.of(2022,3,31,), ErMedlem.PAFOLGENDE),
            Medlemskap("2", LocalDate.of(2022,1,1,), LocalDate.of(2022,1,31,), ErMedlem.UAVKLART),
        )
        var match = finnMatchendeMedlemkapsPeriode(medlemskap, FlexRequest("1",LocalDate.of(2022,1,1),LocalDate.of(2022,1,31)))
        Assertions.assertNotNull(match)
        Assertions.assertEquals(ErMedlem.JA,match!!.medlem)

    }
    @Test
    fun `identisk periode som er paafolgende skal finnes`(){
        val medlemskap = listOf(
            Medlemskap("1", LocalDate.of(2022,1,1,), LocalDate.of(2022,1,31,), ErMedlem.JA),
            Medlemskap("1", LocalDate.of(2022,2,1,), LocalDate.of(2022,2,28,), ErMedlem.PAFOLGENDE),
            Medlemskap("1", LocalDate.of(2022,3,1,), LocalDate.of(2022,3,31,), ErMedlem.PAFOLGENDE),
            Medlemskap("2", LocalDate.of(2022,1,1,), LocalDate.of(2022,1,31,), ErMedlem.UAVKLART),
        )
        var match = finnMatchendeMedlemkapsPeriode(medlemskap, FlexRequest("1",LocalDate.of(2022,3,1),LocalDate.of(2022,3,31)))
        Assertions.assertNotNull(match)
        Assertions.assertEquals(ErMedlem.PAFOLGENDE,match!!.medlem)

    }
    @Test
    fun `ved paafolgende skal forsteIkkePaaFolgendeFinnes`(){
        val medlemskap = listOf(
            Medlemskap("1", LocalDate.of(2021,12,1,), LocalDate.of(2021,12,15,), ErMedlem.UAVKLART),
            Medlemskap("1", LocalDate.of(2022,1,1,), LocalDate.of(2022,1,31,), ErMedlem.JA),
            Medlemskap("1", LocalDate.of(2022,2,1,), LocalDate.of(2022,2,28,), ErMedlem.PAFOLGENDE),
            Medlemskap("1", LocalDate.of(2022,3,1,), LocalDate.of(2022,3,31,), ErMedlem.PAFOLGENDE),
        )
        var match = finnMatchendeMedlemkapsPeriode(medlemskap, FlexRequest("1",LocalDate.of(2022,3,1),LocalDate.of(2022,3,31)))
        Assertions.assertNotNull(match)
        Assertions.assertEquals(ErMedlem.PAFOLGENDE,match!!.medlem)
        val forsteIkkePaafolgende = finnRelevantIkkePåfølgende(match,medlemskap)
        Assertions.assertEquals(ErMedlem.JA,forsteIkkePaafolgende!!.medlem)
        Assertions.assertEquals(LocalDate.of(2022,1,1,),forsteIkkePaafolgende!!.fom)


    }
}