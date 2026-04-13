# Strategiske DDD-mønstre — Referanse

## Innholdsfortegnelse
1. [Context Mapping i praksis](#context-mapping-i-praksis)
2. [Event Storming — utvidet](#event-storming--utvidet)
3. [Subdomain-klassifisering](#subdomain-klassifisering)
4. [Mikrotjeneste-dekomponering](#mikrotjeneste-dekomponering)
5. [Eventuell konsistens og Saga-mønsteret](#eventuell-konsistens-og-saga-mønsteret)
6. [CQRS — Command Query Responsibility Segregation](#cqrs--command-query-responsibility-segregation)

---

## Context Mapping i praksis

### Slik identifiserer du Bounded Contexts

1. **Lytt etter språkskifter** — Når domeneekspertene bruker samme ord med ulik mening, er det
   sannsynligvis en kontekstgrense. F.eks. betyr «søknad» noe annet for den som fyller den ut
   (brukeropplevelse) og den som vurderer den (saksbehandling).

2. **Se etter organisatoriske grenser** — Team som jobber uavhengig har ofte naturlige kontekstgrenser.
   Conway's Law: systemarkitekturen speiler organisasjonen.

3. **Analyser endringsfrekvens** — Deler av domenet som endres sammen hører ofte i samme kontekst.
   Deler som endres uavhengig er kandidater for separate kontekster.

4. **Følg konsistenskravene** — Data som alltid må være konsistent i sanntid hører i samme kontekst.
   Data som kan være eventuelt konsistent kan leve i separate kontekster.

### Relasjonstyper i detalj

#### Partnership
To team samarbeider tett og synkroniserer jevnlig. Begge har innflytelse på den delte modellen.
Fungerer kun med høy tillit og tette kommunikasjonslinjer.

```
[Kontekst A] ←——Partnership——→ [Kontekst B]
```

#### Customer–Supplier
Oppstrøms (supplier) team har sine egne prioriteringer, men tar hensyn til nedstrøms (customer)
behov. Nedstrøms team kan påvirke oppstrøms API, men har ikke vetorett.

```
[Supplier] ——upstream——→ [Customer]
```

#### Conformist
Nedstrøms team adopterer oppstrøms modell som den er, uten å oversette. Enklest mulig
integrasjon, men du kobler deg tett til andres modell.

#### Anti-Corruption Layer (ACL)
Nedstrøms team lager et oversettingslag som konverterer oppstrøms modell til sin egen. Beskytter
domenet mot forurensning fra eksterne modeller.

```kotlin
// ACL-eksempel: oversetter LovMe-respons til vårt domene
class LovMeAntiCorruptionLayer(
    private val lovMeClient: LovMeHttpClient
) : RegelMotor {

    override suspend fun vurder(kommando: VurderMedlemskapKommando): RegelResultat {
        val eksternRespons = lovMeClient.kallLovMe(kommando.tilEksternRequest())
        return eksternRespons.tilDomeneResultat()
    }

    private fun LovMeResponse.tilDomeneResultat(): RegelResultat = when (this.status) {
        "JA" -> RegelResultat.Ja(regelId = this.regelId)
        "NEI" -> RegelResultat.Nei(regelId = this.regelId, årsak = this.årsak)
        else -> RegelResultat.Uavklart(regelId = this.regelId, spørsmål = this.spørsmål)
    }
}
```

#### Open Host Service (OHS) + Published Language
Du tilbyr et veldefinert API (OHS) med en dokumentert kontrakt (Published Language) som
konsumenter kan integrere mot.

```
[Domenet ditt] ——OHS + PL——→ [Konsument A]
                            → [Konsument B]
```

For mikrotjenester på NAIS betyr dette typisk:
- **HTTP/REST** med OpenAPI-spec (Published Language)
- **Kafka-topics** med Avro/JSON Schema (Published Language)

---

## Event Storming — utvidet

### Fargekoder (standard)

| Farge     | Konsept           | Eksempel                            |
|-----------|-------------------|-------------------------------------|
| 🟧 Oransje | Domain Event      | `MedlemskapVurdert`                |
| 🟦 Blå     | Command           | `VurderMedlemskap`                 |
| 🟨 Gul     | Aggregate         | `MedlemskapVurdering`              |
| 🟪 Lilla   | Policy/Reaction   | "Når søknad mottatt → vurder"      |
| 🟩 Grønn   | Read Model        | Vurderingsoversikt for Speil       |
| 🟥 Rød     | Hot Spot/Problem  | "Hva skjer med duplikater?"        |
| ⬜ Hvit    | External System   | LovMe, Flex, Saga                  |

### Prosess

1. **Kaotisk fase** — alle limer opp hendelser de kan tenke seg, uten rekkefølge
2. **Tidslinje** — sortér hendelsene kronologisk
3. **Kommandoer og aktører** — hva trigger hendelsene?
4. **Aggregater** — grupper hendelser og kommandoer rundt aggregater
5. **Bounded Contexts** — tegn grenser rundt relaterte aggregater
6. **Hot spots** — marker problemer, uenigheter, ubesvarte spørsmål

---

## Subdomain-klassifisering

Ikke alt er like viktig. Klassifiser subdomener for å prioritere innsats:

### Core Domain
Det som gir konkurransefortrinn. Her investerer du mest i modellering og kode-kvalitet.
Eksempel: Medlemskapsvurdering (regelmotor, vurderingslogikk).

### Supporting Subdomain
Nødvendig for at Core skal fungere, men ikke differensierende. God nok modellering.
Eksempel: Brukerspørsmål-håndtering, persistering av vurderinger.

### Generic Subdomain
Ingen forretningsverdi å lage selv. Bruk hyllevare eller standardløsninger.
Eksempel: Autentisering (Azure AD), logging, database-migrasjon (Flyway).

```
| Subdomain               | Type        | Strategi                        |
|--------------------------|-------------|----------------------------------|
| Medlemskapsvurdering     | Core        | Rik domenemodell, høy testdekning |
| Søknadsmottak            | Supporting  | Enkel modell, fokus på korrekthet |
| Brukerspørsmål-gjenbruk  | Supporting  | Klar forretningsregel, godt testet |
| Autentisering            | Generic     | Azure AD + NAIS-biblioteker       |
| Databaseadmin            | Generic     | Flyway + standard JDBC            |
```

---

## Mikrotjeneste-dekomponering

### Heuristikker for tjenestegrenser

1. **Én Bounded Context = én mikrotjeneste** (utgangspunkt, ikke absolutt regel)
2. **Single Responsibility** — en tjeneste har én grunn til å endre seg
3. **Uavhengig deploybarhet** — kan du deploye denne tjenesten uten å deploye andre?
4. **Data-eierskap** — hver tjeneste eier sine data. Ingen delt database.
5. **Teamstørrelse** — en tjeneste bør eies av ett team (2-pizza team)

### Når du IKKE bør splitte

- Tjenestene har mye synkron kommunikasjon (chatty services)
- Du trenger distribuerte transaksjoner mellom dem
- Det er samme team som eier begge
- Dataene alltid endres sammen

### Kommunikasjon mellom tjenester

| Metode           | Bruk når                                     | NAV/NAIS-valg     |
|------------------|----------------------------------------------|--------------------|
| Synkron (HTTP)   | Trenger svar umiddelbart, enkel request/reply | Ktor client + TokenX |
| Asynkron (Kafka) | Fire-and-forget, event-drevet arkitektur     | Aiven Kafka         |
| Hybrid           | HTTP for queries, Kafka for commands/events  | Vanlig på NAV       |

---

## Eventuell konsistens og Saga-mønsteret

Når en forretningsprosess spanner flere aggregater eller mikrotjenester, kan du ikke bruke én
transaksjon. I stedet bruker du **Saga-mønsteret**:

### Koreografi-basert saga (event-drevet)
Hver tjeneste lytter på events og reagerer. Ingen sentral koordinator.

```
SøknadMottatt → [Vurderingstjeneste lytter] → VurderMedlemskap
    → MedlemskapVurdert → [Saga-tjeneste lytter] → LagreForFlexOgSpeil
        → VurderingLagret → [Notifikasjonstjeneste lytter] → ...
```

Fordeler: Løs kobling, enkel å utvide
Ulemper: Vanskelig å følge flyten, krever god observerbarhet

### Orkestrering-basert saga
En sentral orkestrator styrer flyten og håndterer kompenserende handlinger ved feil.

```kotlin
class VurderingsSaga(
    private val regelMotor: RegelMotor,
    private val vurderingRepo: MedlemskapVurderingRepository,
    private val sagaClient: SagaClient
) {
    suspend fun utfør(søknad: Søknad) {
        val vurdering = MedlemskapVurdering.opprett(søknad.hashetFnr, søknad.periode)
        try {
            val resultat = regelMotor.vurder(søknad.tilKommando())
            vurdering.registrerResultat(resultat)
            vurderingRepo.lagre(vurdering)
            sagaClient.lagreVurdering(vurdering)
        } catch (e: Exception) {
            // Kompenserende handling
            vurderingRepo.markerFeilet(vurdering.id)
            throw e
        }
    }
}
```

Fordeler: Enklere å forstå flyten, sentral feilhåndtering
Ulemper: Tettere kobling, orkestratoren kan bli en bottleneck

---

## CQRS — Command Query Responsibility Segregation

Skille mellom skrive-modell (commands) og lese-modell (queries). Nyttig når:
- Lese- og skrive-lastprofilen er svært ulik
- Du trenger optimaliserte lese-modeller (denormalisert)
- Du vil skalere lesing og skriving uavhengig

```
                 ┌──────────────────┐
  Commands ──▶   │  Write Model     │ ──▶ Domain Events
                 │  (Aggregater)    │         │
                 └──────────────────┘         │
                                              ▼
                 ┌──────────────────┐    ┌──────────┐
  Queries ◀──    │  Read Model      │ ◀──│ Projector │
                 │  (Views/DTOs)    │    └──────────┘
                 └──────────────────┘
```

For de fleste mikrotjenester på NAV er full CQRS overkill. Men den lette varianten — separate
DTO-er for spørringer vs. domenemodell for skriving — er nesten alltid en god idé.

```kotlin
// Skrivemodell — rikt aggregat
class MedlemskapVurdering { ... }

// Lesemodell — flat DTO optimalisert for API-respons
data class VurderingVisning(
    val id: String,
    val status: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val regelId: String?
)
```

