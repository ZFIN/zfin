--liquibase formatted sql
--changeset rtaylor:ZFIN-10025-add-exp-go-evidence-code

-- ZFIN-10025 / ZFIN-10258: add EXP to go_evidence_code so experimental annotations
-- from the DANRE-mod GPAD load can be stored instead of rejected.
--
-- The chain that currently rejects them:
--   1. the GPAD row carries ECO:0000269 ("experimental evidence used in manual assertion")
--   2. eco_go_mapping already maps ECO:0000269 -> 'EXP'   (GO's canonical mapping)
--   3. GafService:387 looks 'EXP' up in go_evidence_code, gets null, and throws
--      GafValidationError("invalid evidence code: EXP")
--
-- So the only thing missing is the reference-data row -- this is not a parser exclusion.
-- Measured effect: 105 rows per run of the DANRE-mod file. All 105 are
-- assigned_by=UniProt and all are attributed to PMIDs, i.e.
-- literature-backed experimental annotations (verified 2026-07-09). That is why
-- ZFIN-10258 decided to allow them, unlike the GAF path which lists EXP in
-- FpInferenceGafParser.EXCLUDED_EVIDENCE_CODES and drops it silently.
--
-- Note the legacy GAF path is deliberately left alone: it keeps excluding EXP. Only the
-- DANRE-mod path surfaces these rows, because GpadParser bypasses isValidGafEntry.
--
-- goev_display_order: 18, the next free slot (ISO currently holds the highest at 17).
-- goev_name must be unique (go_evidence_code_alternate_key_index).
--
-- Idempotent so a re-run against an already-migrated database is a no-op.

insert into go_evidence_code (goev_code, goev_name, goev_display_order)
  select 'EXP', 'inferred from experiment', 18
   where not exists (select 1 from go_evidence_code where goev_code = 'EXP');
