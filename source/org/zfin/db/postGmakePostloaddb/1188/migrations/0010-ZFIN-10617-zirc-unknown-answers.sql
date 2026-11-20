--liquibase formatted sql

-- ZFIN-10617: "Unknown" answer for Homozygous Lethal and Is phenotype
-- background dependent. Stored beside the nullable boolean (rather than in it)
-- so NULL keeps meaning unanswered. The CHECK constraint holds the pairing:
-- an "unknown" answer never carries a yes/no value as well.
--
-- Written to be re-runnable over a database that already has the columns in
-- their earlier nullable form.

--changeset rtaylor:ZFIN-10617-zirc-unknown-answers
ALTER TABLE zirc.mutation
    ADD COLUMN IF NOT EXISTS m_homozygous_lethal_unknown BOOLEAN;
UPDATE zirc.mutation SET m_homozygous_lethal_unknown = false
    WHERE m_homozygous_lethal_unknown IS NULL;
ALTER TABLE zirc.mutation
    ALTER COLUMN m_homozygous_lethal_unknown SET DEFAULT false,
    ALTER COLUMN m_homozygous_lethal_unknown SET NOT NULL,
    DROP CONSTRAINT IF EXISTS m_homozygous_lethal_unknown_excl,
    ADD CONSTRAINT m_homozygous_lethal_unknown_excl
        CHECK (NOT m_homozygous_lethal_unknown OR m_homozygous_lethal IS NULL);

ALTER TABLE zirc.phenotype
    ADD COLUMN IF NOT EXISTS p_background_dependent_unknown BOOLEAN;
UPDATE zirc.phenotype SET p_background_dependent_unknown = false
    WHERE p_background_dependent_unknown IS NULL;
ALTER TABLE zirc.phenotype
    ALTER COLUMN p_background_dependent_unknown SET DEFAULT false,
    ALTER COLUMN p_background_dependent_unknown SET NOT NULL,
    DROP CONSTRAINT IF EXISTS p_background_dependent_unknown_excl,
    ADD CONSTRAINT p_background_dependent_unknown_excl
        CHECK (NOT p_background_dependent_unknown OR p_background_dependent IS NULL);
--rollback ALTER TABLE zirc.mutation DROP COLUMN IF EXISTS m_homozygous_lethal_unknown;
--rollback ALTER TABLE zirc.phenotype DROP COLUMN IF EXISTS p_background_dependent_unknown;
