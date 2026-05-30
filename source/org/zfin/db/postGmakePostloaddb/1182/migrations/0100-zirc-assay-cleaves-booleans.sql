--liquibase formatted sql

-- Replace ga_enzyme_cleaves (text[] of "positions where the enzyme cuts")
-- with two boolean columns mirroring the old form: one per template.
-- Curators tick a checkbox to indicate the enzyme cleaves the template;
-- they don't enter sequences anymore. No backfill — the dropped column
-- only had smoke-test data.

--changeset zirc:zirc-assay-cleaves-booleans
ALTER TABLE zirc.genotyping_assay DROP COLUMN ga_enzyme_cleaves;
ALTER TABLE zirc.genotyping_assay
    ADD COLUMN ga_enzyme_cleaves_wt  BOOLEAN,
    ADD COLUMN ga_enzyme_cleaves_mut BOOLEAN;
