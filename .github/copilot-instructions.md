# Copilot Instructions – medlemskap-sykepengerlytter

## Build, test, and lint commands

```bash
./gradlew test                                                        # run all tests
./gradlew test --tests "no.nav.medlemskap.sykepenger.lytter.service.SoknadRecordHandlerTest"  # single test class
./gradlew shadowJar                                                   # build fat JAR (output: build/libs/app.jar)
```

Tests require Docker (Testcontainers uses PostgreSQL and Kafka containers). Set `KAFKA_ENABLED=false` (already the default) to skip Kafka bootstrap during local runs.

---

## Architecture overview

This is a **Kotlin/Ktor** microservice on the NAV NAIS platform (GCP) that bridges Flex (sick-pay application UI) and the membership assessment engine (LovMe / `medl-oppslag`).

### Two entry points

1. **Kafka consumer** (`BrukerSporsmaalConsumer`) – listens to `flex.sykepengesoknad` (Aiven Kafka, SSL). Messages flow:
   - `FlexMessageHandler` filters for `ARBEIDSTAKERE` / `GRADERT_REISETILSKUDD` and `SENDT` status.
   - Two parallel actions: persisting `brukerspørsmål` (user answers) to PostgreSQL, and forwarding to `SoknadRecordHandler` for a LovMe call.
   - `SoknadRecordHandler` deduplicates and skips consecutive (`påfølgende`) søknader before calling LovMe.

2. **HTTP server** (Ktor/Netty) – three authenticated endpoints (Azure AD JWT required):
   - `POST /flexvurdering` – returns a stored membership assessment for a Flex request.
   - `POST /speilvurdering` – returns a speil-specific assessment; falls back to a live LovMe call if not in DB.
   - `GET /brukersporsmal?fom=&tom=` + `fnr` header – runs a LovMe call and returns which questions should be shown in the søknad UI, with reuse logic for answers < 32 days old.

### External dependencies

| Service | Purpose |
|---------|---------|
| `medl-oppslag` (LovMe) | Membership rule engine – called for every søknad |
| `saga` | Secondary store for retrieving/persisting assessments used by Flex/Speil |
| Kafka `flex.sykepengesoknad` | Source of sykepengesøknader |

### Persistence (PostgreSQL + Flyway)

Two tables (see `src/main/resources/db/migration/`):
- `syk_vurdering` – one row per membership assessment (`id`, hashed `fnr`, `fom`, `tom`, `status`).
- `brukersporsmaal` – user answers per søknad; `sporsmaal` column is stored as `jsonb`.

**FNR is always SHA-256 hashed** before being written to the database (see `security/Encryption.kt`). Never store or log raw FNR.

---

## Key conventions

### Privacy / logging
- Sensitive logs use `MarkerFactory.getMarker("TEAM_LOGS")` – these are separated at the log-aggregation layer.
- Log structured fields with `kv("key", value)` from `net.logstash.logback.argument.StructuredArguments`.
- FNR must go through `fnr.sha256()` before any DB write.

### Configuration
- All config is read from environment variables via `com.natpryce:konfig` (`Configuration.kt`).
- Safe local defaults are provided in `defaultProperties` inside `Configuration.kt`; do not hard-code secrets.
- `KAFKA_ENABLED=false` disables the Kafka consumer (useful for local HTTP-only testing).

### Testing patterns
- Unit tests use **in-memory repository stubs** (`MedlemskapVurdertInMemmoryRepository`, `BrukersporsmaalInMemmoryRepository`) instead of Testcontainers.
- Integration/repository tests extend `AbstractContainerDatabaseTest` and use `@Testcontainers` + real PostgreSQL.
- Coroutine-based service methods are tested with `runBlocking { }`.
- Mock external HTTP clients with `io.mockk` or `ktor-client-mock`.

### Søknad filtering rules
- Only `ARBEIDSTAKERE` and `GRADERT_REISETILSKUDD` søknad types are processed.
- Only `SENDT` status is persisted/forwarded.
- `SoknadRecordHandler.isDuplikat()` skips functionally identical søknader (same fnr/fom/tom).
- `SoknadRecordHandler.isPaafolgendeSoknad()` skips follow-up søknads unless `arbeidUtenforNorge=true`.

### Brukerspørsmål reuse logic (≤ 32 days)
Rules for which questions Flex should show again (see `tjenester.md` for formal notation):
- If new potential questions **equal** existing answers → ask nothing.
- If existing answers are a **strict subset** of potential questions → ask all potential questions.
- If potential questions are a **subset** of existing answers → ask nothing.
