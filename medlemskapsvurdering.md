

Uvaklart
```
{
  "kanal": "kafka",
  "resultat": {
    "svar": "UAVKLART",
    "dekning": "",
    "regelId": "REGEL_MEDLEM_KONKLUSJON",
    "årsaker": [
      {
        "svar": "NEI",
        "regelId": "REGEL_3",
        "avklaring": "Har bruker hatt et sammenhengende arbeidsforhold i Aa-registeret de siste 12 månedene?",
        "begrunnelse": "Arbeidstaker har ikke sammenhengende arbeidsforhold siste 12 mnd"
      },
      {
        "svar": "JA",
        "regelId": "REGEL_9",
        "avklaring": "Har bruker utført arbeid utenfor Norge?",
        "begrunnelse": ""
      }
    ],
    "avklaring": "Er bruker medlem?",
    "harDekning": null,
    "begrunnelse": "Kan ikke konkludere med medlemskap",
    "delresultat": [
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_DOED",
        "årsaker": [],
        "avklaring": "Er det avklart om brukeren er død eller ikke?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_13",
            "årsaker": [],
            "avklaring": "Er bruker død?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_FELLES_ARBEIDSFORHOLD",
        "årsaker": [],
        "avklaring": "Er felles regler for arbeidsforhold avklart?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_17",
            "årsaker": [],
            "avklaring": "Har bruker arbeidsforhold?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_17_1",
            "årsaker": [],
            "avklaring": "Er bruker frilanser?",
            "harDekning": null,
            "begrunnelse": "Bruker er ikke frilanser",
            "delresultat": []
          },
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_22",
            "årsaker": [],
            "avklaring": "Arbeidsforhold siste 12 måneder: finnes det utenlandsopphold i et av disse arbeidsforholdene?",
            "harDekning": null,
            "begrunnelse": "Det finnes ikke utenlandsopphold i et av arbeidsforholdene de siste 12 månedene",
            "delresultat": []
          },
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_21",
            "årsaker": [],
            "avklaring": "Er bruker arbeidstaker i kontrollperiode for stønadsområde?",
            "harDekning": null,
            "begrunnelse": "Bruker er arbeidstaker i kontrollperiode for stønadsområde",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_STATSBORGERSKAP",
        "årsaker": [],
        "avklaring": "Er statsborgerskap avklart?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_2",
            "årsaker": [],
            "avklaring": "Er bruker omfattet av grunnforordningen (EØS)? Dvs er bruker statsborger i et EØS-land inkl. Norge?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_11",
            "årsaker": [],
            "avklaring": "Er bruker norsk statsborger?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_MEDL",
        "årsaker": [],
        "avklaring": "Har bruker avklarte opplysninger i MEDL?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_OPPLYSNINGER",
            "årsaker": [],
            "avklaring": "Finnes det registrerte opplysninger på bruker?",
            "harDekning": null,
            "begrunnelse": "Alle de følgende ble NEI",
            "delresultat": [
              {
                "svar": "NEI",
                "dekning": "",
                "regelId": "REGEL_A",
                "årsaker": [],
                "avklaring": "Finnes det registrerte opplysninger i MEDL?",
                "harDekning": null,
                "begrunnelse": "",
                "delresultat": []
              },
              {
                "svar": "NEI",
                "dekning": "",
                "regelId": "REGEL_C",
                "årsaker": [],
                "avklaring": "Finnes det dokumenter i JOARK på medlemskapsområdet?",
                "harDekning": null,
                "begrunnelse": "",
                "delresultat": []
              },
              {
                "svar": "NEI",
                "dekning": "",
                "regelId": "REGEL_B",
                "årsaker": [],
                "avklaring": "Finnes det åpne oppgaver i GOSYS på medlemskapsområdet?",
                "harDekning": null,
                "begrunnelse": "",
                "delresultat": []
              }
            ]
          }
        ]
      },
      {
        "svar": "UAVKLART",
        "dekning": "",
        "regelId": "REGEL_ARBEIDSFORHOLD",
        "årsaker": [
          {
            "svar": "NEI",
            "regelId": "REGEL_3",
            "avklaring": "Har bruker hatt et sammenhengende arbeidsforhold i Aa-registeret de siste 12 månedene?",
            "begrunnelse": "Arbeidstaker har ikke sammenhengende arbeidsforhold siste 12 mnd"
          }
        ],
        "avklaring": "Er arbeidsforhold avklart?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_3",
            "årsaker": [],
            "avklaring": "Har bruker hatt et sammenhengende arbeidsforhold i Aa-registeret de siste 12 månedene?",
            "harDekning": null,
            "begrunnelse": "Arbeidstaker har ikke sammenhengende arbeidsforhold siste 12 mnd",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_BOSATT",
        "årsaker": [],
        "avklaring": "Er det avklart om bruker bor i Norge?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_10",
            "årsaker": [],
            "avklaring": "Er bruker folkeregistrert som bosatt i Norge og har vært det i 12 mnd?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "UAVKLART",
        "dekning": "",
        "regelId": "REGEL_NORSK",
        "årsaker": [
          {
            "svar": "JA",
            "regelId": "REGEL_9",
            "avklaring": "Har bruker utført arbeid utenfor Norge?",
            "begrunnelse": ""
          }
        ],
        "avklaring": "Er regler for norske borgere avklart?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_9",
            "årsaker": [],
            "avklaring": "Har bruker utført arbeid utenfor Norge?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          }
        ]
      }
    ]
  },
  "tidspunkt": "2023-11-24T11:56:36.647081",
  "konklusjon": [
    {
      "dato": "2023-11-24",
      "hvem": "SP6000",
      "status": "UAVKLART",
      "lovvalg": null,
      "medlemskap": null,
      "dekningForSP": "UAVKLART",
      "reglerKjørt": [
        {
          "svar": "NEI",
          "dekning": "",
          "regelId": "SP6001",
          "årsaker": [],
          "avklaring": "Skal regelmotor prosessere gammel kjøring",
          "harDekning": null,
          "begrunnelse": "Årsaker i gammel kjøring tilsier ikke at hale skal utføres",
          "delresultat": [],
          "utledetInformasjon": []
        }
      ],
      "avklaringsListe": [
        {
          "hvem": "SP6000",
          "svar": "NEI",
          "status": "UAVKLART",
          "regel_id": "REGEL_3",
          "tidspunkt": "2023-11-24",
          "beskrivelse": null,
          "avklaringstekst": "Har bruker hatt et sammenhengende arbeidsforhold i Aa-registeret de siste 12 månedene?"
        },
        {
          "hvem": "SP6000",
          "svar": "JA",
          "status": "UAVKLART",
          "regel_id": "REGEL_9",
          "tidspunkt": "2023-11-24",
          "beskrivelse": null,
          "avklaringstekst": "Har bruker utført arbeid utenfor Norge?"
        }
      ],
      "utledetInformasjoner": [
        {
          "kilde": [
            "REGEL_11"
          ],
          "informasjon": "NORSK_BORGER"
        },
        {
          "kilde": [
            "REGEL_2"
          ],
          "informasjon": "EØS_BORGER"
        }
      ]
    }
  ],
  "datagrunnlag": {
    "fnr": "10507213737",
    "ytelse": "SYKEPENGER",
    "periode": {
      "fom": "2023-11-01",
      "tom": "2023-11-23"
    },
    "dokument": [],
    "oppgaver": [],
    "dataOmBarn": [],
    "medlemskap": [],
    "brukerinput": {
      "oppholdUtenforEos": {
        "id": "6daa94ba-df7f-35cd-8634-5f453ff56a08",
        "svar": true,
        "sporsmalstekst": "Har du oppholdt deg utenfor EØS i løpet av de siste 12 månedene før du ble syk?",
        "oppholdUtenforEOS": [
          {
            "id": "426701f4-f06b-306b-ac51-024d6d318986",
            "land": "Afghanistan",
            "grunn": "Jeg var med ektefelle/samboer som jobbet der",
            "perioder": [
              {
                "fom": "2023-11-05",
                "tom": "2023-11-09"
              }
            ]
          }
        ]
      },
      "oppholdstilatelse": null,
      "arbeidUtenforNorge": true,
      "oppholdUtenforNorge": null,
      "utfortAarbeidUtenforNorge": {
        "id": "1b5b87e2-7d83-350e-86dc-2e53b2e23099",
        "svar": true,
        "sporsmalstekst": "Har du arbeidet utenfor Norge i løpet av de siste 12 månedene før du ble syk?",
        "arbeidUtenforNorge": [
          {
            "id": "32e1124e-2292-3089-8c24-40f6231ee4a1",
            "land": "Afghanistan",
            "perioder": [
              {
                "fom": "2023-11-05",
                "tom": "2023-11-08"
              }
            ],
            "arbeidsgiver": "Leger uten grenser"
          }
        ]
      }
    },
    "arbeidsforhold": [
      {
        "periode": {
          "fom": "2023-07-01",
          "tom": null
        },
        "arbeidsgiver": {
          "ansatte": null,
          "konkursStatus": null,
          "juridiskeEnheter": [
            {
              "enhetstype": "AS",
              "antallAnsatte": null,
              "organisasjonsnummer": "963743254"
            }
          ],
          "organisasjonsnummer": "972674818"
        },
        "arbeidsavtaler": [
          {
            "periode": {
              "fom": "2023-11-24",
              "tom": null
            },
            "skipstype": null,
            "yrkeskode": "3310101",
            "fartsomraade": null,
            "skipsregister": null,
            "stillingsprosent": 100.0,
            "gyldighetsperiode": {
              "fom": "2023-07-01",
              "tom": null
            },
            "beregnetAntallTimerPrUke": 37.5
          }
        ],
        "arbeidsgivertype": "Organisasjon",
        "utenlandsopphold": null,
        "arbeidsforholdstype": "NORMALT",
        "permisjonPermittering": null
      }
    ],
    "dataOmEktefelle": null,
    "overstyrteRegler": {},
    "oppholdstillatelse": null,
    "pdlpersonhistorikk": {
      "navn": [
        {
          "fornavn": "ÖKÄND",
          "etternavn": "FORNØYELSE",
          "mellomnavn": null
        }
      ],
      "doedsfall": [],
      "sivilstand": [],
      "bostedsadresser": [
        {
          "fom": "1972-10-10",
          "tom": null,
          "landkode": "NOR",
          "historisk": false
        }
      ],
      "kontaktadresser": [],
      "statsborgerskap": [
        {
          "fom": "1972-10-10",
          "tom": null,
          "landkode": "NOR",
          "historisk": false
        }
      ],
      "oppholdsadresser": [],
      "utflyttingFraNorge": [],
      "innflyttingTilNorge": [],
      "forelderBarnRelasjon": []
    },
    "startDatoForYtelse": "2023-11-01",
    "førsteDagForYtelse": "2023-11-01"
  },
  "versjonRegler": "v1",
  "versjonTjeneste": "94b5ef9"
}
```


Ja
```
{
  "kanal": "kafka",
  "resultat": {
    "svar": "JA",
    "dekning": "",
    "regelId": "REGEL_MEDLEM_KONKLUSJON",
    "årsaker": [],
    "avklaring": "Er bruker medlem?",
    "harDekning": null,
    "begrunnelse": "Bruker er medlem",
    "delresultat": [
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_DOED",
        "årsaker": [],
        "avklaring": "Er det avklart om brukeren er død eller ikke?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_13",
            "årsaker": [],
            "avklaring": "Er bruker død?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_FELLES_ARBEIDSFORHOLD",
        "årsaker": [],
        "avklaring": "Er felles regler for arbeidsforhold avklart?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_17",
            "årsaker": [],
            "avklaring": "Har bruker arbeidsforhold?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_17_1",
            "årsaker": [],
            "avklaring": "Er bruker frilanser?",
            "harDekning": null,
            "begrunnelse": "Bruker er ikke frilanser",
            "delresultat": []
          },
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_22",
            "årsaker": [],
            "avklaring": "Arbeidsforhold siste 12 måneder: finnes det utenlandsopphold i et av disse arbeidsforholdene?",
            "harDekning": null,
            "begrunnelse": "Det finnes ikke utenlandsopphold i et av arbeidsforholdene de siste 12 månedene",
            "delresultat": []
          },
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_21",
            "årsaker": [],
            "avklaring": "Er bruker arbeidstaker i kontrollperiode for stønadsområde?",
            "harDekning": null,
            "begrunnelse": "Bruker er arbeidstaker i kontrollperiode for stønadsområde",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_STATSBORGERSKAP",
        "årsaker": [],
        "avklaring": "Er statsborgerskap avklart?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_2",
            "årsaker": [],
            "avklaring": "Er bruker omfattet av grunnforordningen (EØS)? Dvs er bruker statsborger i et EØS-land inkl. Norge?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_11",
            "årsaker": [],
            "avklaring": "Er bruker norsk statsborger?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_MEDL",
        "årsaker": [],
        "avklaring": "Har bruker avklarte opplysninger i MEDL?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_OPPLYSNINGER",
            "årsaker": [],
            "avklaring": "Finnes det registrerte opplysninger på bruker?",
            "harDekning": null,
            "begrunnelse": "Alle de følgende ble NEI",
            "delresultat": [
              {
                "svar": "NEI",
                "dekning": "",
                "regelId": "REGEL_A",
                "årsaker": [],
                "avklaring": "Finnes det registrerte opplysninger i MEDL?",
                "harDekning": null,
                "begrunnelse": "",
                "delresultat": []
              },
              {
                "svar": "NEI",
                "dekning": "",
                "regelId": "REGEL_C",
                "årsaker": [],
                "avklaring": "Finnes det dokumenter i JOARK på medlemskapsområdet?",
                "harDekning": null,
                "begrunnelse": "",
                "delresultat": []
              },
              {
                "svar": "NEI",
                "dekning": "",
                "regelId": "REGEL_B",
                "årsaker": [],
                "avklaring": "Finnes det åpne oppgaver i GOSYS på medlemskapsområdet?",
                "harDekning": null,
                "begrunnelse": "",
                "delresultat": []
              }
            ]
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_ARBEIDSFORHOLD",
        "årsaker": [],
        "avklaring": "Er arbeidsforhold avklart?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_3",
            "årsaker": [],
            "avklaring": "Har bruker hatt et sammenhengende arbeidsforhold i Aa-registeret de siste 12 månedene?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_4",
            "årsaker": [],
            "avklaring": "Er foretaket registrert i foretaksregisteret?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_14",
            "årsaker": [],
            "avklaring": "Er bruker ansatt i staten eller i en kommune?",
            "harDekning": null,
            "begrunnelse": "Bruker er ikke ansatt i staten eller i en kommune",
            "delresultat": []
          },
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_5",
            "årsaker": [],
            "avklaring": "Har arbeidsgiver sin hovedaktivitet i Norge?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_6",
            "årsaker": [],
            "avklaring": "Er foretaket aktivt?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_7",
            "årsaker": [],
            "avklaring": "Er arbeidsforholdet maritimt?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_8",
            "årsaker": [],
            "avklaring": "Er bruker pilot eller kabinansatt?",
            "harDekning": null,
            "begrunnelse": "Bruker er ikke pilot eller kabinansatt",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_BOSATT",
        "årsaker": [],
        "avklaring": "Er det avklart om bruker bor i Norge?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_10",
            "årsaker": [],
            "avklaring": "Er bruker folkeregistrert som bosatt i Norge og har vært det i 12 mnd?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          }
        ]
      },
      {
        "svar": "JA",
        "dekning": "",
        "regelId": "REGEL_NORSK",
        "årsaker": [],
        "avklaring": "Er regler for norske borgere avklart?",
        "harDekning": null,
        "begrunnelse": "",
        "delresultat": [
          {
            "svar": "NEI",
            "dekning": "",
            "regelId": "REGEL_9",
            "årsaker": [],
            "avklaring": "Har bruker utført arbeid utenfor Norge?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          },
          {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_12",
            "årsaker": [],
            "avklaring": "Har bruker vært i minst 25% stilling de siste 12 mnd?",
            "harDekning": null,
            "begrunnelse": "",
            "delresultat": []
          }
        ]
      }
    ]
  },
  "tidspunkt": "2023-11-23T15:09:24.454949",
  "konklusjon": [
    {
      "dato": "2023-11-23",
      "hvem": "SP6000",
      "status": "JA",
      "lovvalg": null,
      "medlemskap": {
        "erMedlem": "JA",
        "ftlHjemmel": ""
      },
      "dekningForSP": "UAVKLART",
      "reglerKjørt": [
        {
          "svar": "NEI",
          "dekning": "",
          "regelId": "SP6001",
          "årsaker": [],
          "avklaring": "Skal regelmotor prosessere gammel kjøring",
          "harDekning": null,
          "begrunnelse": "Årsaker i gammel kjøring tilsier ikke at hale skal utføres",
          "delresultat": [],
          "utledetInformasjon": []
        }
      ],
      "avklaringsListe": [],
      "utledetInformasjoner": [
        {
          "kilde": [
            "REGEL_11"
          ],
          "informasjon": "NORSK_BORGER"
        },
        {
          "kilde": [
            "REGEL_2"
          ],
          "informasjon": "EØS_BORGER"
        }
      ]
    }
  ],
  "datagrunnlag": {
    "fnr": "11897898049",
    "ytelse": "SYKEPENGER",
    "periode": {
      "fom": "2023-09-01",
      "tom": "2023-09-30"
    },
    "dokument": [],
    "oppgaver": [],
    "dataOmBarn": [],
    "medlemskap": [],
    "brukerinput": {
      "oppholdUtenforEos": null,
      "oppholdstilatelse": null,
      "arbeidUtenforNorge": false,
      "oppholdUtenforNorge": null,
      "utfortAarbeidUtenforNorge": null
    },
    "arbeidsforhold": [
      {
        "periode": {
          "fom": "2003-11-23",
          "tom": null
        },
        "arbeidsgiver": {
          "ansatte": null,
          "konkursStatus": null,
          "juridiskeEnheter": [
            {
              "enhetstype": "AS",
              "antallAnsatte": null,
              "organisasjonsnummer": "963743254"
            }
          ],
          "organisasjonsnummer": "896929119"
        },
        "arbeidsavtaler": [
          {
            "periode": {
              "fom": "2023-11-23",
              "tom": null
            },
            "skipstype": null,
            "yrkeskode": "2521106",
            "fartsomraade": null,
            "skipsregister": null,
            "stillingsprosent": 100.0,
            "gyldighetsperiode": {
              "fom": "2003-11-01",
              "tom": null
            },
            "beregnetAntallTimerPrUke": 37.5
          }
        ],
        "arbeidsgivertype": "Organisasjon",
        "utenlandsopphold": null,
        "arbeidsforholdstype": "NORMALT",
        "permisjonPermittering": null
      }
    ],
    "dataOmEktefelle": null,
    "overstyrteRegler": {},
    "oppholdstillatelse": null,
    "pdlpersonhistorikk": {
      "navn": [
        {
          "fornavn": "KLAR",
          "etternavn": "BADEBY",
          "mellomnavn": null
        }
      ],
      "doedsfall": [],
      "sivilstand": [
        {
          "type": "GIFT",
          "gyldigFraOgMed": "2002-11-12",
          "gyldigTilOgMed": null,
          "relatertVedSivilstand": "30908098575"
        }
      ],
      "bostedsadresser": [
        {
          "fom": "2022-02-08",
          "tom": null,
          "landkode": "NOR",
          "historisk": false
        }
      ],
      "kontaktadresser": [],
      "statsborgerskap": [
        {
          "fom": "1978-09-11",
          "tom": null,
          "landkode": "NOR",
          "historisk": false
        }
      ],
      "oppholdsadresser": [],
      "utflyttingFraNorge": [],
      "innflyttingTilNorge": [],
      "forelderBarnRelasjon": [
        {
          "minRolleForPerson": "BARN",
          "relatertPersonsIdent": "21885198047",
          "relatertPersonsRolle": "MOR"
        },
        {
          "minRolleForPerson": "MOR",
          "relatertPersonsIdent": "06810298535",
          "relatertPersonsRolle": "BARN"
        },
        {
          "minRolleForPerson": "MOR",
          "relatertPersonsIdent": "02831099278",
          "relatertPersonsRolle": "BARN"
        },
        {
          "minRolleForPerson": "BARN",
          "relatertPersonsIdent": "13815397767",
          "relatertPersonsRolle": "FAR"
        },
        {
          "minRolleForPerson": "MOR",
          "relatertPersonsIdent": "10810399053",
          "relatertPersonsRolle": "BARN"
        },
        {
          "minRolleForPerson": "MOR",
          "relatertPersonsIdent": "26921198956",
          "relatertPersonsRolle": "BARN"
        }
      ]
    },
    "startDatoForYtelse": "2023-09-01",
    "førsteDagForYtelse": "2023-09-01"
  },
  "versjonRegler": "v1",
  "versjonTjeneste": "94b5ef9"
}
```