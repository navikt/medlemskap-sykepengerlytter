CREATE TABLE IF NOT EXISTS syk_vurdering
(
    id VARCHAR(100),
    fnr VARCHAR(100),
    fom date,
    tom date,
    status VARCHAR(10)

);

CREATE  INDEX IF NOT EXISTS fnr_idx ON syk_vurdering (fnr);