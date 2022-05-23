package no.nav.medlemskap.sykepenger.lytter.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DatePickerTest {
    @Test
    fun `minste dato skal velges`(){
        val sendtNav = LocalDate.MIN
        val sendtArbeidsgiver = LocalDate.MAX
        Assertions.assertEquals(sendtNav,DatePicker().findEarliest(sendtNav,sendtArbeidsgiver))
    }
    @Test
    fun `null verdi i sendtNav skal ikke velges`(){
        val sendtNav = null
        val sendtArbeidsgiver = LocalDate.MAX
        Assertions.assertEquals(sendtArbeidsgiver,DatePicker().findEarliest(sendtNav,sendtArbeidsgiver))
    }
    @Test
    fun `null verdi i sendtArbeidsgiver skal ikke velges`(){
        val sendtNav = LocalDate.MAX
        val sendtArbeidsgiver = null
        Assertions.assertEquals(sendtNav,DatePicker().findEarliest(sendtNav,sendtArbeidsgiver))
    }
    @Test
    fun `Begge null skal ha dagens dato`(){
        val sendtNav = null
        val sendtArbeidsgiver = null
        Assertions.assertEquals(LocalDate.now(),DatePicker().findEarliest(sendtNav,sendtArbeidsgiver))
    }
}