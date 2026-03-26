# medlemskap-sykepengerlytter
komponent for lytting på sykepenger-søknad-kafkaTopic


## URL til tjeneste
* dev: https://medlemskap-vurdering-sykepenger.intern.dev.nav.no/flexvurdering  -- POST
* dev: https://medlemskap-vurdering-sykepenger.intern.dev.nav.no/speilvurdering  -- POST
* dev: https://medlemskap-vurdering-sykepenger.intern.dev.nav.no/brukersporsmal?fom=<dato>&tom=<dato> -- GET
* prod: https://medlemskap-vurdering-sykepenger.intern.nav.no/flexvurdering  -- POST
* prod: https://medlemskap-vurdering-sykepenger.intern.nav.no/speilvurdering  -- POST
* prod: https://medlemskap-vurdering-sykepenger.intern.nav.no/brukersporsmal?fom=<>&tom=<>  -- GET


## URL til test-tjeneste
Denne tjenesten er kun tilgjengelig i dev. Hensikten er å kunne teste å sende søknader til LovMe uten Kafka.

* dev: https://medlemskap-vurdering-sykepenger.intern.dev.nav.no/test/publiser-sykepengesoknad  -- POST

Se [egen dokumentasjon](flex-melding-felter.md) som beskriver eksempler på meldinger som kan sendes til LovMe.

## Autentisering
Forventer et AzureAD-token utstedt til servicebruker, satt Authorization-header (Bearer)

## Azure AD Scope
| Azure scope                                                | Miljø    |
|------------------------------------------------------------|----------|
| api://dev-gcp.medlemskap-sykepenger-listener/.default  | GCP-DEV  |
| api://prod-gcp.medlemskap-sykepenger-listener/.default | GCP-PROD |

## Headere
I tillegg til Authorization-headeren kreves det at Content-Type er satt til application/json
for brukerspørsmål kreves også at fnr er satt i header


## Eksempel request (flexvurdering)
```
{
    "sykepengesoknad_id":"b2b4b4bbddjb4brn3b",
    "fnr":"12345678901",
    "fom":"2022-04-07",
    "tom":"2022-05-03"
}
```
## Eksempel respons (flexvurdering)
```
{
    "sykepengesoknad_id": "bff75jfu7584urkr84ur9",
    "vurdering_id": "123",
    "fnr": "12345678901",
    "fom": "2022-03-10",
    "tom": "2022-04-06",
    "status": "JA"
}
```

## Eksempel request (speilvurdering)
```
{
  "fnr" : "12345678765",
  "førsteDagForYtelse" : "2023-11-01",
  "periode" : {
    "fom" : "2023-11-01",
    "tom" : "2023-11-27"
  },
  "ytelse" : "SYKEPENGER"
}

```
## Eksempel respons (speilvurdering)
```
{
  "soknadId" : "ed0286f6-6107-3d75-8266-e50d5736f403",
  "fnr" : "12345678765",
  "speilSvar" : "UAVKLART_MED_BRUKERSPORSMAAL"
}
```
# Avhengigheter
* Kafka
* Medlemskap-Oppslag

# Revisjonslogging
Revisjonslogging for databasen `sykmedlemskap` er aktivert.
Loggene er tilgjengelig inne på instancen i [Nais Console](https://console.nav.cloud.nais.io/team/medlemskap/postgres)

# Database
medlemskap-sykepengerlytter har en database som heter `sykmedlemskap` som inneholder tabellene `brukersporsmaal`
og `syk_vurdering`. Databasen er tilgjengelig i både dev-gcp og prod-gcp, og det benyttes Flyway for migrering av databasen.


## Steg 0: Få personlig tilgang (gjøres kun 1 gang ⚠️)
Gir brukeren din tilgang til databasen. Dette steget trengs kun å gjøres en gang. Dersom brukeren din
allerede har tilgang, kan du hoppe over dette steget og gå videre til steg 1.
```bash
nais postgres grant --team medlemskap --environment dev-gcp medlemskap-sykepenger-listener
```

## Steg 1: Velg riktig tilgangsnivå

Velg **én** av følgende basert på behov:

### Steg 1a – Lesetilgang (standard)
Brukes for feilsøking og innsikt i data:

```bash
nais postgres prepare --team medlemskap --environment dev-gcp medlemskap-sykepenger-listener
```

### Steg 1b – Skrivetilgang (kun ved behov ⚠️)

Brukes kun når det er nødvendig å endre data:
```bash
nais postgres prepare --team medlemskap --environment dev-gcp medlemskap-sykepenger-listener --all-privileges
```

## Steg 2: Koble til databasen
```bash
nais postgres proxy --team medlemskap --environment dev-gcp --reason "debugging issues" medlemskap-sykepenger-listener
```

## Steg 3: Rydd opp etter deg ⚠️
Når du er ferdig skal du fjerne tilgangsnivået du fikk tildelt i Steg 1.
```bash
nais postgres revoke --team medlemskap --environment dev-gcp medlemskap-sykepenger-listener
```

**Er du usikker på hvilken tilgang du har?**

Du kan sjekke hvilke rettigheter som din bruker har ved å kjøre følgende SQL-spørring i databasen:

```bash
SELECT
    has_table_privilege(current_user, 'brukersporsmaal', 'SELECT') AS can_select,
    has_table_privilege(current_user, 'brukersporsmaal', 'INSERT') AS can_insert,
    has_table_privilege(current_user, 'brukersporsmaal', 'UPDATE') AS can_update,
    has_table_privilege(current_user, 'brukersporsmaal', 'DELETE') AS can_delete;
```

**Teamets ansvar ved skrivetilgang ⚠️**

I teamet skal du ikke ha behov for å gi deg selv skrivetilgang til _prod-gcp_. Hvis du har behov for det, skal
teamet informeres og tilgangen skal loggføres i [adgangsoversikten](http://confluence.adeo.no/spaces/TLM/pages/800081767/Loggf%C3%B8ring+av+skrivetilgang+til+databaser+i+produksjon) med dato og tjenestelig formål.
