# AGENTS.md

Guide for coding agents working in `navikt/medlemskap-sykepengerlytter`.

## Purpose

Kotlin/Ktor service that connects Flex sykepengesøknad events to membership assessment (`medl-oppslag` / LovMe), and exposes HTTP endpoints for Flex/Speil lookups.

## Quick start (local)

- Required: JDK 21, Docker (for Testcontainers-based tests)
- Optional for HTTP-only local runs: `KAFKA_ENABLED=false` (default)

Use Java 21 explicitly if your default JDK is newer:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

## Build, test, package

```bash
./gradlew test
./gradlew test --tests "no.nav.medlemskap.sykepenger.lytter.service.SoknadRecordHandlerTest"
./gradlew shadowJar
```

CI (`.github/workflows/pull-request.yml`, `.github/workflows/master.yml`) runs:

```bash
./gradlew test shadowJar
```

## Architecture at a glance

### Entry points
1. Kafka consumer: `BrukerSporsmaalConsumer`
   - Subscribes to `flex.sykepengesoknad`
   - Uses `FlexMessageHandler` and `SoknadRecordHandler`
   - Filters by type/status and handles persistence + LovMe flow
2. HTTP server (Ktor)
   - `POST /flexvurdering`
   - `POST /speilvurdering`
   - `GET /brukersporsmal?fom=&tom=` (requires `fnr` header)

### Persistence
- PostgreSQL + Flyway migrations (`src/main/resources/db/migration`)
- Main tables:
  - `syk_vurdering`
  - `brukersporsmaal` (`sporsmaal` as `jsonb`)

## Security and privacy constraints (must follow)

- Never store or log raw FNR
- Always hash FNR before DB writes (`fnr.sha256()`)
- Use sensitive logging marker `TEAM_LOGS` where applicable
- Use structured logging via `kv("key", value)`

## Kafka and config notes

- Config from env via `Configuration.kt` (`konfig`)
- Local defaults are in `defaultProperties` in `Configuration.kt`
- Kafka custom config/security in:
  - `src/main/kotlin/no/nav/medlemskap/sykepenger/lytter/config/KafkaConfig.kt`
  - `src/main/kotlin/no/nav/medlemskap/sykepenger/lytter/config/PlainStrategy.kt`

## Domain rules agents must preserve

- Process only søknad types: `ARBEIDSTAKERE`, `GRADERT_REISETILSKUDD`
- Process only status: `SENDT`
- Skip duplicates and påfølgende søknader according to `SoknadRecordHandler` rules
- Brukerspørsmål reuse window: 32 days (subset/equality rules)

## Testing patterns

- Prefer in-memory repositories in unit tests
- Use Testcontainers-based tests only where integration behavior is required
- Coroutine service tests typically use `runBlocking {}`
- Mock external HTTP calls with `io.mockk` or `ktor-client-mock`

## Change policy for agents

- Make minimal, targeted changes
- Follow existing code style/patterns
- Do not introduce new dependencies unless clearly necessary
- Do not change unrelated files or behavior
- Validate with focused tests first, then broader tests as needed

## Useful file map

- Runtime config: `src/main/kotlin/no/nav/medlemskap/sykepenger/lytter/config/Configuration.kt`
- Kafka consumer: `src/main/kotlin/no/nav/medlemskap/sykepenger/lytter/BrukerSporsmaalConsumer.kt`
- Kafka config/security: `src/main/kotlin/no/nav/medlemskap/sykepenger/lytter/config/KafkaConfig.kt`, `src/main/kotlin/no/nav/medlemskap/sykepenger/lytter/config/PlainStrategy.kt`
- Core handling: `src/main/kotlin/no/nav/medlemskap/sykepenger/lytter/service/FlexMessageHandler.kt`, `src/main/kotlin/no/nav/medlemskap/sykepenger/lytter/service/SoknadRecordHandler.kt`
- Project instructions: `.github/copilot-instructions.md`
