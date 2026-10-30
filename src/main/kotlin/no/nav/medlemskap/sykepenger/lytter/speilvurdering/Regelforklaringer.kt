package no.nav.medlemskap.sykepenger.lytter.speilvurdering

private const val STANDARD_FORKLARING = "Kontakt Team LovMe for mer informasjon."

private val regelforklaringer = mapOf(
    "REGEL_0_1" to STANDARD_FORKLARING,
    "REGEL_0_5" to STANDARD_FORKLARING,
    "REGEL_1_1" to "Det er minst én periode som er \"uavklart\", innenfor de siste 12 månedene, " +
        "se under \"Medlemskap\" i Gosys",
    "REGEL_1_2" to "Det er perioder med og uten medlemskap, innenfor de siste 12 månedene, " +
        "se under \"Medlemskap\" i Gosys",
    "REGEL_1_3" to STANDARD_FORKLARING,
    "REGEL_1_3_1" to "Det er en periode uten medlemskap som ikke dekker de siste 12 månedene. " +
        "Se hvilken arbeidsgiver og periode vedtaket om unntak fra medlemskap gjelder for, " +
        "og om dette stemmer overens med informasjonen i Aa-registeret. " +
        "Du må vurdere hvilken periode bruker har unntak fra medlemskap.",
    "REGEL_1_3_3" to "Det finnes et vedtak om unntak fra medlemskap i Norge etter USA eller Canadaavtalen. " +
        "Disse to avtalene omfatter ikke sykepenger. " +
        "Du må vurdere om bruker er begrenset medlem i folketrygden for de ytelsene som ikke omfattes av trygdeavtalen.",
    "REGEL_1_3_4" to STANDARD_FORKLARING,
    "REGEL_1_3_5" to "Det finnes et vedtak om unntak fra medlemskap. " +
        "Se hvilken arbeidsgiver og periode vedtaket gjelder for, " +
        "og om dette stemmer overens med informasjonen i Aa-registeret. " +
        "Du må vurdere hvilken periode bruker har unntak fra medlemskap.",
    "REGEL_1_4" to "Det er en periode med medlemskap som ikke dekker de siste 12 månedene, " +
        "se under \"Medlemskap\" i Gosys",
    "REGEL_1_5" to "Det er en periode med medlemskap som dekker de siste 12 månedene, " +
        "samtidig som bruker har endret arbeidsforhold i Aa-registeret i løpet av perioden. " +
        "Se hvilken arbeidsgiver vedtaket om medlemskap gjelder for.",
    "REGEL_1_6" to "Kontakt Team LovMe for mer informasjon.",
    "REGEL_1_7" to "Det finnes et vedtak om medlemskap og det kan se ut som bruker har et begrenset medlemskap " +
        "som ikke omfatter sykepenger, se under \"Medlemskap\" i Gosys og les selve vedtaket.",
    "REGEL_2" to STANDARD_FORKLARING,
    "REGEL_3" to "Bruker har ikke hatt et sammenhengende arbeidsforhold i Aa-registeret de siste 12 månedene.",
    "REGEL_4" to STANDARD_FORKLARING,
    "REGEL_5" to "Bruker har et arbeidsforhold hos en arbeidsgiver med mindre enn seks ansatte. " +
        "Det er mulig arbeidsgiver ikke har sin hovedaktivitet i Norge.",
    "REGEL_6" to STANDARD_FORKLARING,
    "REGEL_7" to STANDARD_FORKLARING,
    "REGEL_7_1" to STANDARD_FORKLARING,
    "REGEL_8" to "Bruker er pilot eller kabinansatt. Det er mulig bruker har hjemmebase utenfor Norge.",
    "REGEL_9" to "Bruker har svart at hen har utført arbeid utenfor Norge i søknad om sykepenger. " +
        "Det finnes ingen tilleggsinformasjon i søknaden.",
    "REGEL_10" to "Bruker har ikke vært sammenhengende folkeregistrert, som bosatt i Norge, " +
        "de siste 12 månedene.",
    "REGEL_11" to STANDARD_FORKLARING,
    "REGEL_11_2" to STANDARD_FORKLARING,
    "REGEL_11_2_1" to STANDARD_FORKLARING,
    "REGEL_11_2_2" to "Bruker har barn både i Norge og i utlandet. " +
        "Vurder om bruker kan ha et annet land enn Norge som lovvalgsland.",
    "REGEL_11_2_2_1" to "Regelen har tre alternative årsaker til Uavklart. " +
        "Du må derfor sjekke hvor eventuell ektefelle og eventuelle barn er folkeregistrert som bosatt. " +
        "Bruker har ikke hatt 100% stilling de siste 12 månedene. " +
        "Vurder om bruker kan ha et annet land enn Norge som lovvalgsland.",
    "REGEL_11_2_2_2" to STANDARD_FORKLARING,
    "REGEL_11_2_3" to """
        Bruker har ikke en ektefelle i folkeregisteret
        Bruker har barn som er folkeregisteret i Norge
        Bruker har hatt mindre enn 80% stilling de siste 12 månedene.
        Vurder om bruker kan ha et annet land enn Norge som lovvalgsland.
    """.trimIndent(),
    "REGEL_11_3" to STANDARD_FORKLARING,
    "REGEL_11_3_1" to STANDARD_FORKLARING,
    "REGEL_11_3_1_1" to """
        Bruker har en ektefelle som ikke er bosatt i Norge
        Bruker har ikke barn i folkeregisteret
        Bruker har ikke hatt 100% stilling de siste 12 månedene.
        Vurder om bruker kan ha et annet land enn Norge som lovvalgsland.
    """.trimIndent(),
    "REGEL_11_4" to STANDARD_FORKLARING,
    "REGEL_11_4_1" to "Bruker har barn både i Norge og i utlandet. " +
        "Vurder om bruker kan ha et annet land enn Norge som lovvalgsland.",
    "REGEL_11_4_2" to "Bruker har barn både i Norge og i utlandet. " +
        "Bruker har ikke hatt 100% stilling de siste 12 månedene.",
    "REGEL_11_5" to "Bruker har barn både i Norge og i utlandet. " +
        "Vurder om bruker kan ha et annet land enn Norge som lovvalgsland.",
    "REGEL_11_5_1" to "Ektefellen og barna bor antakelig ikke i samme land. " +
        "Vurder om bruker kan ha et annet land enn Norge som lovvalgsland.",
    "REGEL_11_5_2" to STANDARD_FORKLARING,
    "REGEL_11_5_3" to STANDARD_FORKLARING,
    "REGEL_11_6" to STANDARD_FORKLARING,
    "REGEL_11_6_1" to """
        Bruker har en ektefelle som er folkeregistrert som bosatt i Norge. Ektefellen har ikke hatt 100 % stilling de siste 12 månedene.
        Alt 1: Bruker har ikke barn i folkeregisteret / Alt 2: Bruker har barn men de er ikke folkeregistrert som bosatt i Norge
        Bruker har ikke hatt 80% stilling de siste 12 månedene.
        Vurder om bruker kan ha et annet land enn Norge som lovvalgsland.
    """.trimIndent(),
    "REGEL_12" to "Bruker har ikke hatt minst 25% stilling de siste 12 månedene. " +
        "Vurder om bruker kan ha et annet land enn Norge som lovvalgsland, " +
        "ved å se om bruker har fått uavklart på en SP-regel.",
    "REGEL_13" to "Bruker er død.",
    "REGEL_14" to STANDARD_FORKLARING,
    "REGEL_15" to "Bruker har vært permittert i løpet av de siste 12 månedene.",
    "REGEL_17" to "Bruker har ikke arbeidsforhold registrert i Aa-registeret.",
    "REGEL_16" to "Bruker har et frilans arbeidsforhold.",
    "REGEL_18" to STANDARD_FORKLARING,
    "REGEL_19_1" to "UDI kunne ikke bekrefte at bruker har gyldig oppholdstillatelse da LovMe gjorde vurderingen. " +
        "Kontroller brukers oppholdstillatelse og arbeidsadgang.",
    "REGEL_19_2" to "Bruker har to ulike typer oppholdstillatelse som overlapper. " +
        "Kontroller brukers oppholdstillatelse og arbeidsadgang.",
    "REGEL_19_3" to "UDI kan ikke bekrefte at bruker hadde gyldig oppholdstillatelse " +
        "12 måneder tilbake og 2 måneder frem i tid. Kontroller brukers oppholdstillatelse og arbeidsadgang.",
    "REGEL_19_4" to "Bruker registrert med oppholdstype \"EØS eller EFTA familie\", " +
        "men ektefelle er statsborger i UK.",
    "REGEL_19_5" to "UDI-tjenesten kan ikke bekrefte at bruker har gyldig arbeidsadgang. " +
        "Kontroller brukers oppholdstillatelse og arbeidsadgang.",
    "REGEL_19_6" to "UDI kan ikke bekrefte at bruker hadde gyldig arbeidsadgang " +
        "12 måneder tilbake og 2 måneder frem i tid. Kontroller brukers oppholdstillatelse og arbeidsadgang.",
    "REGEL_19_6_1" to "UDI kan ikke bekrefte at bruker har hatt gyldig arbeidsadgangen " +
        "hele perioden bruker har vært i arbeid i Norge. Kontroller brukers oppholdstillatelse og arbeidsadgang.",
    "REGEL_19_8" to "UDI oppgir at bruker har en søknad om oppholdstillatelse til behandling. " +
        "Kontroller brukers oppholdstillatelse og arbeidsadgang.",
    "REGEL_21" to "Bruker har ikke vært registrert registrert i Aa-registeret " +
        "med arbeidsforhold de siste 12 månedene.",
    "REGEL_22" to STANDARD_FORKLARING,
    "REGEL_23" to "UDI oppga at bruker ikke hadde gyldig oppholdstillatelse da LovMe gjorde vurderingen. " +
        "Kontroller brukers oppholdstillatelse og arbeidsadgang.",
    "REGEL_25" to "Arbeidsgiver har registrert i Aa-registeret at bruker har vært i arbeid i utlandet i løpet av de siste 12 månedene.",
    "REGEL_27" to "Bruker har blitt norsk statsborger i løpet av de siste 12 månedene. " +
        "Kontroller brukers oppholdstillatelse og arbeidsadgang frem til bruker ble norsk statsborger.",
    "REGEL_28" to STANDARD_FORKLARING,
    "REGEL_29" to STANDARD_FORKLARING,
    "REGEL_30" to """
        Bruker er statsborger i UK, men har oppholdstype "EØS eller EFTA"

        Kontroller brukers oppholdstillatelse og arbeidsadgang.
    """.trimIndent(),
    "REGEL_31" to STANDARD_FORKLARING,
    "REGEL_32" to STANDARD_FORKLARING,
    "REGEL_33" to STANDARD_FORKLARING,
    "REGEL_35" to STANDARD_FORKLARING,
    "REGEL_36" to "Bruker har et maritimt arbeidsforhold på vurderingstidspunktet, " +
        "men har ikke hatt et sammenhengende maritimt arbeidsforhold på et NOR-skip de siste 12 månedene.",
    "REGEL_42" to "Bruker har ikke et maritimt arbeidsforhold på vurderingstidspunktet, " +
        "men har hatt et maritimt arbeidsforhold i løpet av de siste 12 månedene.",
    "REGEL_50" to STANDARD_FORKLARING,
    "REGEL_51" to "Bruker har hatt flere permisjoner de siste 12 månedene. " +
        "Bruker har hatt mer enn 60 dager permisjon.",
    "REGEL_54" to STANDARD_FORKLARING,
    "REGEL_55" to "Bruker har hatt én permisjonsperiode de siste 12 månedene. " +
        "Bruker har hatt mer enn 60 dager permisjon.",
    "REGEL_57" to STANDARD_FORKLARING,
    "REGEL_58" to "Bruker har hatt én permisjonsperiode de siste 12 månedene. " +
        "Bruker har hatt mer enn 15 dager permisjon. Siste permisjonsdag var for mindre enn 30 dager siden.",
    "REGEL_60" to STANDARD_FORKLARING,
    "REGEL_61" to "Bruker har bare hatt ett arbeidsforhold de siste 12 månedene. " +
        "Dette arbeidsforholdet har ikke hatt en varighet på minst 12 måneder. " +
        "Bruker er medlem etter § 2-2, men LovMe kan ikke vurdere om bruker er medlem som bosatt etter § 2-1. " +
        "Bruker er statsborger i et land utenfor EØS.",
    "REGEL_62" to "Bruker er statsborger i et land utenfor EØS, " +
        "og har ikke hatt minst 60 % stilling de siste 12 månedene.",
    "REGEL_64" to "Bruker har bare hatt ett arbeidsforhold de siste 12 månedene. " +
        "Bruker har hatt mindre enn 60 % stilling de siste 12 månedene. " +
        "Bruker er medlem etter § 2-2 i arbeidsperiodene, " +
        "men LovMe kan ikke vurdere om bruker er medlem som bosatt etter § 2-1. " +
        "Bruker er statsborger i et land utenfor EØS",
    "REGEL_65" to STANDARD_FORKLARING,
    "REGEL_66" to "Bruker har bare hatt ett arbeidsforhold de siste 12 månedene. " +
        "I dette arbeidsforholdet har bruker flere arbeidsavtaler. " +
        "Bruker er medlem etter § 2-2 i arbeidsperiodene, " +
        "men LovMe kan ikke vurdere om bruker er medlem som bosatt etter § 2-1. " +
        "Bruker er statsborger i et land utenfor EØS.",
    "REGEL_70" to "Bruker har oppholdstillatelsestype \"EØS eller EFTA\" " +
        "og UDI kan ikke bekrefte at oppholdstilatelsen er gyldig 12 måneder tilbake og 2 måneder frem i tid.",
    "SP6001" to STANDARD_FORKLARING,
    "SP6110" to STANDARD_FORKLARING,
    "SP6120" to STANDARD_FORKLARING,
    "SP6130" to STANDARD_FORKLARING,
    "SP6201" to STANDARD_FORKLARING,
    "SP6211" to STANDARD_FORKLARING,
    "SP6212" to STANDARD_FORKLARING,
    "SP6221" to STANDARD_FORKLARING,
    "SP6222" to STANDARD_FORKLARING,
    "SP6223" to STANDARD_FORKLARING,
    "SP6225" to STANDARD_FORKLARING,
    "SP6226" to STANDARD_FORKLARING,
    "SP6229_1" to STANDARD_FORKLARING,
    "SP6301" to STANDARD_FORKLARING,
    "SP6311" to STANDARD_FORKLARING,
    "SP6312" to STANDARD_FORKLARING,
    "SP6313" to STANDARD_FORKLARING,
    "SP6314" to STANDARD_FORKLARING,
    "SP6321" to STANDARD_FORKLARING,
    "SP6401" to STANDARD_FORKLARING,
    "SP6411" to STANDARD_FORKLARING,
    "SP6412" to STANDARD_FORKLARING,
    "SP6413" to STANDARD_FORKLARING,
    "SP6414" to STANDARD_FORKLARING,
    "SP6510" to STANDARD_FORKLARING,
    "SP6600" to STANDARD_FORKLARING,
    "SP6700" to STANDARD_FORKLARING
)

fun tilAarsak(regelId: String): Aarsak =
    Aarsak(
        regelId = regelId,
        forklaringPaaUavklart = regelforklaringer[regelId] ?: STANDARD_FORKLARING
    )
