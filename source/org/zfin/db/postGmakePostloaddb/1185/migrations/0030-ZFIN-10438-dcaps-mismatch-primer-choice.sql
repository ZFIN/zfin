--liquibase formatted sql

-- ZFIN-10438: the dCAPS "Primer with introduced mismatch" field changes meaning.
-- It used to be a free-text box for the mismatch primer's sequence; the mockup
-- turns it into a closed dropdown answering *which* primer carries the
-- mismatch, Forward or Reverse.
--
-- That is a different kind of value, so it gets its own column rather than
-- reusing ga_dcaps_mismatch_primer:
--
--   * the old column's name describes a primer sequence, and would be a lie
--     once it held "Forward"
--   * any sequence already recorded would become invalid data in a field that
--     now means something else
--
-- ga_dcaps_mismatch_primer is therefore left in place and simply retired from
-- the form. Nothing reads it any more, but whatever a submitter typed is still
-- there and recoverable with SQL. Locally it holds nothing at all: 2 assays
-- exist, none with a mismatch value and none of type dcaps.
--
-- Plain TEXT holding 'Forward' or 'Reverse'. No CHECK constraint: the closed
-- list lives in the form schema (ZircAssayFormSchema), and a database
-- constraint would have to be migrated in lockstep with any future label
-- change for no benefit.

--changeset cmpich:ZFIN-10438-dcaps-mismatch-primer-choice
ALTER TABLE zirc.genotyping_assay
    ADD COLUMN IF NOT EXISTS ga_dcaps_mismatch_primer_choice TEXT;
--rollback ALTER TABLE zirc.genotyping_assay DROP COLUMN IF EXISTS ga_dcaps_mismatch_primer_choice;
