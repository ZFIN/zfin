--liquibase formatted sql

-- B10a: validation-polish schema changes on zirc.genotyping_assay.
--
-- ga_restriction_enzyme is split into name + catalog per the PDF spec
-- ("name, catalog number (Andrzej has a macvector file)"). The form
-- previously took a single free-text string; production-style entries
-- need both a vendor name and a SKU.
--
-- ga_enzyme_cleaves widens from a single string to text[] so the
-- curator can mark both WT and MUT when the enzyme cleaves both
-- templates ("Allow selecting both" from the PDF). Mirrors the
-- text[] pattern already in use for phenotype.segregation / type and
-- the parent submission's ls_reasons.
--
-- No prod data — safe to drop the old columns rather than carrying
-- a backfill.

--changeset rtaylor:zirc-b10a-assay-validation
ALTER TABLE zirc.genotyping_assay
    DROP COLUMN ga_restriction_enzyme,
    DROP COLUMN ga_enzyme_cleaves,
    ADD COLUMN ga_restriction_enzyme_name    TEXT,
    ADD COLUMN ga_restriction_enzyme_catalog TEXT,
    ADD COLUMN ga_enzyme_cleaves             TEXT[] NOT NULL DEFAULT '{}';
