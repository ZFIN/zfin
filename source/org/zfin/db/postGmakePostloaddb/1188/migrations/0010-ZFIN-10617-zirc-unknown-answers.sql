--liquibase formatted sql

-- ZFIN-10617: "Unknown" answer for Homozygous Lethal and Is phenotype
-- background dependent. Stored beside the nullable boolean (rather than in it)
-- so NULL keeps meaning unanswered; unknown = true implies the boolean is NULL.

--changeset rtaylor:ZFIN-10617-zirc-unknown-answers
ALTER TABLE zirc.mutation
    ADD COLUMN IF NOT EXISTS m_homozygous_lethal_unknown BOOLEAN;
ALTER TABLE zirc.phenotype
    ADD COLUMN IF NOT EXISTS p_background_dependent_unknown BOOLEAN;
--rollback ALTER TABLE zirc.mutation DROP COLUMN IF EXISTS m_homozygous_lethal_unknown;
--rollback ALTER TABLE zirc.phenotype DROP COLUMN IF EXISTS p_background_dependent_unknown;
