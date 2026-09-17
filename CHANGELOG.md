# Changelog

## Unreleased

- Sentraliserte byggingen av applikasjonens komponentgraf i `ApplicationComponents`.
- Delte database- og klientinstanser mellom HTTP-serveren og Kafka-behandlingen.
- Gjorde Kafka-consumerens domenetjenester eksplisitt injiserte.
- Erstattet `GlobalScope` med en applikasjonseid coroutine-scope med kontrollert shutdown.
