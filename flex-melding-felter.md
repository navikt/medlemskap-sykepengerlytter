# Felter fra Flex-melding brukt av `FlexMessageHandler.handle()`

Oversikt over hvilke JSON-felter fra Kafka-meldingen (`value`) som faktisk leses og brukes
gjennom hele behandlingskjeden.

---

## Toppnivå-felter

| Felt | Type | Brukt til |
|------|------|-----------|
| `id` | `String` | Brukes som `soknadid` ved lagring av brukerspørsmål, som `callId` i LovMe-kall og logging |
| `type` | `String` (enum) | Filtrering – kun `ARBEIDSTAKERE` og `GRADERT_REISETILSKUDD` behandles |
| `status` | `String` (enum) | Filtrering – kun `SENDT`-søknader lagres og sendes til LovMe |
| `fnr` | `String` | Lagres på brukerspørsmål-raden; brukes i duplikatsjekk og LovMe-request |
| `fom` | `LocalDate` | Startdato for sykepengeperioden – sendes til LovMe og brukes i påfølgende-sjekk |
| `tom` | `LocalDate` | Sluttdato for sykepengeperioden – sendes til LovMe og brukes i påfølgende-sjekk |
| `sendtArbeidsgiver` | `LocalDateTime?` | Brukes til å sette `eventDate` (tidligste av `sendtArbeidsgiver` / `sendtNav`) |
| `sendtNav` | `LocalDateTime?` | Brukes til å sette `eventDate` (tidligste av `sendtArbeidsgiver` / `sendtNav`) |
| `dodsdato` | `String?` | Hvis satt: alle brukerspørsmål settes til `null` |
| `ettersending` | `Boolean?` | Søknaden filtreres ut og sendes ikke til LovMe dersom `true` |
| `arbeidUtenforNorge` | `Boolean?` | Avgjør om en påfølgende/duplikat-søknad likevel skal sendes til LovMe |
| `forstegangssoknad` | `Boolean?` | Dersom `true` hoppes påfølgende-sjekken over |
| `korrigerer` | `String?` | Leses av Jackson (del av `LovmeSoknadDTO`), men brukes ikke aktivt i logikken |
| `startSyketilfelle` | `LocalDate?` | Leses av Jackson, men brukes ikke aktivt i logikken |

---

## `sporsmal`-array

Feltet `sporsmal` er en liste av spørsmål. Kun spørsmål med bestemte `tag`-verdier leses:

| `tag` | Lagres som | Beskrivelse |
|-------|-----------|-------------|
| `ARBEID_UTENFOR_NORGE` | `sporsmaal.arbeidUtland` | Gammel modell – JA/NEI-svar på om bruker har jobbet i utlandet siste 12 mnd |
| `MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE` | `utfort_arbeid_utenfor_norge` | Ny modell – strukturert arbeid utført utenfor Norge med arbeidsgiver, land og periode |
| `MEDLEMSKAP_OPPHOLD_UTENFOR_NORGE` | `oppholdUtenforNorge` | Opphold utenfor Norge med land, grunn og periode |
| `MEDLEMSKAP_OPPHOLD_UTENFOR_EOS` | `oppholdUtenforEOS` | Opphold utenfor EØS med land, grunn og periode |
| `MEDLEMSKAP_OPPHOLDSTILLATELSE` | `oppholdstilatelse` | Oppholdstillatelse (gammel modell) med vedtaksdato, type og perioder |
| `MEDLEMSKAP_OPPHOLDSTILLATELSE_V2` | `oppholdstilatelse` | Oppholdstillatelse (ny modell v2) – prioriteres foran gammel modell |

Alle andre `tag`-verdier i `sporsmal`-arrayen ignoreres.

---

## JSON-eksempel med kun `MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE` (svar: NEI)

```json
{
  "id": "52041604-a94a-38ca-b7a6-3e913b5207fa",
  "type": "ARBEIDSTAKERE",
  "status": "SENDT",
  "fnr": "12345678901",
  "fom": "2024-01-01",
  "tom": "2024-01-31",
  "startSyketilfelle": "2024-01-01",
  "sendtArbeidsgiver": "2024-01-31T12:00:00.000000",
  "sendtNav": null,
  "dodsdato": null,
  "ettersending": false,
  "arbeidUtenforNorge": false,
  "forstegangssoknad": true,
  "korrigerer": null,
  "sporsmal": [
    {
      "id": "b6c1e327-7454-327f-8ae2-67b7c61df1b6",
      "tag": "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE",
      "sporsmalstekst": "Har du utført arbeid utenfor Norge i de siste 12 månedene?",
      "undertekst": null,
      "min": null,
      "max": null,
      "svartype": "JA_NEI",
      "kriterieForVisningAvUndersporsmal": "JA",
      "svar": [
        {
          "verdi": "NEI"
        }
      ],
      "undersporsmal": []
    }
  ]
}
```

---

## JSON-eksempel med `MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE` (svar: JA)

Når bruker svarer JA fylles `undersporsmal` ut med én gruppering per arbeidsforhold utenfor Norge.

```json
{
  "id": "52041604-a94a-38ca-b7a6-3e913b5207fa",
  "type": "ARBEIDSTAKERE",
  "status": "SENDT",
  "fnr": "12345678901",
  "fom": "2024-01-01",
  "tom": "2024-01-31",
  "startSyketilfelle": "2024-01-01",
  "sendtArbeidsgiver": "2024-01-31T12:00:00.000000",
  "sendtNav": null,
  "dodsdato": null,
  "ettersending": false,
  "arbeidUtenforNorge": true,
  "forstegangssoknad": true,
  "korrigerer": null,
  "sporsmal": [
    {
      "id": "b6c1e327-7454-327f-8ae2-67b7c61df1b6",
      "tag": "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE",
      "sporsmalstekst": "Har du utført arbeid utenfor Norge i de siste 12 månedene?",
      "undertekst": null,
      "min": null,
      "max": null,
      "svartype": "JA_NEI",
      "kriterieForVisningAvUndersporsmal": "JA",
      "svar": [
        {
          "verdi": "JA"
        }
      ],
      "undersporsmal": [
        {
          "id": "cc4735f1-a808-346b-b511-361ba001c8d1",
          "tag": "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_GRUPPERING_0",
          "sporsmalstekst": null,
          "undertekst": null,
          "min": null,
          "max": null,
          "svartype": "IKKE_RELEVANT",
          "kriterieForVisningAvUndersporsmal": null,
          "svar": [],
          "undersporsmal": [
            {
              "id": "5f7bedcd-278b-319c-8d26-7b5963117047",
              "tag": "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_HVOR_0",
              "sporsmalstekst": "Velg land",
              "undertekst": null,
              "min": null,
              "max": null,
              "svartype": "COMBOBOX_SINGLE",
              "kriterieForVisningAvUndersporsmal": null,
              "svar": [
                {
                  "verdi": "Sverige"
                }
              ],
              "undersporsmal": []
            },
            {
              "id": "c2d975c0-5ff7-3437-b5c5-5b5094cddadb",
              "tag": "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_ARBEIDSGIVER_0",
              "sporsmalstekst": "Arbeidsgiver",
              "undertekst": null,
              "min": "1",
              "max": "200",
              "svartype": "FRITEKST",
              "kriterieForVisningAvUndersporsmal": null,
              "svar": [
                {
                  "verdi": "IKEA Sverige AB"
                }
              ],
              "undersporsmal": []
            },
            {
              "id": "ec559a54-b7fa-321e-8c8f-892fbc5dede9",
              "tag": "MEDLEMSKAP_UTFORT_ARBEID_UTENFOR_NORGE_NAAR_0",
              "sporsmalstekst": null,
              "undertekst": null,
              "min": "2014-01-31",
              "max": "2025-01-31",
              "svartype": "PERIODE",
              "kriterieForVisningAvUndersporsmal": null,
              "svar": [
                {
                  "verdi": "{\"fom\":\"2023-06-01\",\"tom\":\"2023-08-31\"}"
                }
              ],
              "undersporsmal": []
            }
          ]
        }
      ]
    }
  ]
}
```


---

## JSON-eksempel med kun `ARBEID_UTENFOR_NORGE`

```json
{
  "id": "52041604-a94a-38ca-b7a6-3e913b5207fa",
  "type": "ARBEIDSTAKERE",
  "status": "SENDT",
  "fnr": "12345678901",
  "fom": "2024-01-01",
  "tom": "2024-01-31",
  "startSyketilfelle": "2024-01-01",
  "sendtArbeidsgiver": "2024-01-31T12:00:00.000000",
  "sendtNav": null,
  "dodsdato": null,
  "ettersending": false,
  "arbeidUtenforNorge": true,
  "forstegangssoknad": true,
  "korrigerer": null,
  "sporsmal": [
    {
      "id": "9d0f36d1-8ba4-3cff-b745-af459666281f",
      "tag": "ARBEID_UTENFOR_NORGE",
      "sporsmalstekst": "Har du arbeidet i utlandet i løpet av de siste 12 månedene?",
      "undertekst": null,
      "min": null,
      "max": null,
      "svartype": "JA_NEI",
      "kriterieForVisningAvUndersporsmal": null,
      "svar": [
        {
          "verdi": "JA"
        }
      ],
      "undersporsmal": []
    }
  ]
}
```
