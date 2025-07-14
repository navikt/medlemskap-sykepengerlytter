package no.nav.medlemskap.sykepenger.lytter.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RegelMotorResponsHandlerTest {

    @Test
    fun regel10_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_10.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_10")
    }


    @Test
    fun regel15_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_15.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_15")
    }

    @Test
    fun kombinasjonerAvEnkeltReglerIListaSkalFøreTilBrukerSpørsmål() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_3_OG_REGEL_34.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(
            respons.sporsmal.isEmpty(),
            "Det skal opprettes brukersporsmaal paa kobinasjoner av godkjente regler"
        )
    }

    @Test
    fun kombinasjonerAvEnkeltReglerOgMultiReglerIListaSkalFøreTilBrukerSpørsmål() {
        val fileContent =
            this::class.java.classLoader.getResource("REGEL_3_OG_REGEL_11_2_2_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(
            respons.sporsmal.isEmpty(),
            "Det skal opprettes brukersporsmaal paa kobinasjoner av godkjente regler"
        )
    }

    @Test
    fun Ulovlig_kombinasjonerAvEnkeltReglerOgMultiReglerIListaSkalIkkeFøreTilBrukerSpørsmål() {
        val fileContent =
            this::class.java.classLoader.getResource("REGEL_3_8_OG_11_2_2_1.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertTrue(
            respons.sporsmal.isEmpty(),
            "Det skal ikke opprettes brukersporsmaal paa ulovlige kombinasjoner av ikke godkjente regler"
        )
    }

    @Test
    fun regelC_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_C.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_C")
    }

    @Test
    fun regel11_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_11_2_3.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_11_2_3")
    }

    @Test
    fun regel12_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_12.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_12")
    }

    @Test
    fun regel20_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_20.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_20")
    }

    @Test
    fun regel34_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_34.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_34")
    }

    @Test
    fun regel21_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_21.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_21")
    }

    @Test
    fun regel25_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_25.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_25")
    }

    @Test
    fun regel5_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_5.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_5")
    }

    @Test
    fun regel5_i_kombinasjon_med_regel3_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_5_og_10.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_5 med REGEL_10")
    }

    @Test
    fun regel23_i_kombinasjon_med_regel3_skal_fore_til_bruersporsmaal() {
        val fileContent = this::class.java.classLoader.getResource("REGEL_23.json").readText(Charsets.UTF_8)
        val respons = RegelMotorResponsHandler().utledResultat(fileContent)
        Assertions.assertFalse(respons.sporsmal.isEmpty(), "Skal opprettes brukersporsmaal paa REGEL_5 med REGEL_10")
    }

}