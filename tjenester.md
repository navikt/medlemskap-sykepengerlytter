# Sykepengelytter - Integrasjon mot Flex og Speil

Sykepengelytter håndterer integrasjoner og forretningslogikk knyttet til medlemskapsvurdering i forbindelse med
sykepengesøknader.  
Tjenesten brukes av **Flex** og **Speil**, og dekker fire hovedtyper av forespørsler.

---

## Oversikt over ansvar

Sykepengelytter håndterer følgende funksjonalitet:

1. Forespørsel om brukerspørsmål fra Flex
2. Søknad om sykemelding fra Flex
3. Forespørsel fra Speil om medlemskapsvurdering
4. Forespørsel om status på endelig medlemskapsvurdering fra Flex

## Brukerspørsmål

Flex bruker denne funksjonaliteten for å finne ut **hvilke brukerspørsmål som skal stilles i søknaden**.

### Gammelt brukerspørsmål:

1. "Arbeid utland"

### Nye brukerspørsmål:

1. "Utført arbeid utenfor Norge"
2. "Opphold utenfor Norge"
3. "Opphold utenfor EØS"
4. "Oppholdstillatelse"

### Levetid på brukerspørsmål

**Hensikt:**

- Sørge for at Flex alltid får komplette spørsmålssett anbefalt.
- Sikre at den nyeste informasjonen hentes gjennom brukerspørsmålene
- Unngå å stille brukeren de samme spørsmålene flere ganger.
- Dersom de potensielle spørsmålene ikke tilfører noe nytt sammenlignet med tidligere stilte spørsmål, stilles ingen
  spørsmål på nytt.
- Forbedre brukeropplevelsen i søknadsprosessen.
- Levetid på gjenbruk er mindre enn 32 dager.

**Flyt:**

- Flex sender forespørsel om hvilke brukerspørsmål som skal stilles i søknaden.
- Sykepengelytter kjører medlemskapsvurdering i medlemskap-oppslag for å finne **potensielle brukerspørsmål**
- Sykepengelytter sjekker om det finnes tidligere brukersvar som er nyere enn 32 dager.
- Hvis det finnes tidligere brukersvar, samles disse til et sett av **foreslåtte spørsmål** som kan være aktuelle å
  stille på nytt.
- Sykepengelytter vurderer hvilke spørsmål som skal stilles basert på potensielle spørsmål og foreslåtte spørsmål.

**Regler:**

- P = F → ∅
    - Hvis de potensielle spørsmålene er identiske med de som allerede er stilt, skal ingen spørsmål stilles på nytt.
- F ⊂ P → P
    - Hvis alle tidligere stilte spørsmål finnes i de potensielle, og det finnes nye spørsmål i tillegg, skal alle
      potensielle spørsmål stilles.
- P ⊆ F → ∅
    - Hvis alle potensielle spørsmål allerede er stilt tidligere (men ikke nødvendigvis omvendt), skal ingen spørsmål
      stilles på nytt.

### Gjenbruk av brukersvar

Brukersvar er svarene som brukeren har gitt på de spørsmålene som er stilt i søknaden. Sykepengelytter lagrer disse svarene i en database, og bruker dem som grunnlag for å vurdere hvilke spørsmål som skal stilles på nytt i fremtidige søknader.


//TODO
