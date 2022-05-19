CREATE TABLE IF NOT EXISTS brukersporsmaal
(
    fnr VARCHAR(100),
    soknadid VARCHAR(100),
    eventDate date,
    ytelse VARCHAR(30),
    status VARCHAR(10),
    sporsmaal jsonb

);

CREATE  INDEX IF NOT EXISTS brukersporsmaal_fnr_idx ON brukersporsmaal (fnr);