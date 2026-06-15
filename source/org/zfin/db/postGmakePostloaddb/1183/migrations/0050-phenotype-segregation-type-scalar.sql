--liquibase formatted sql

-- Phenotype segregation / type are single-valued (one inheritance pattern,
-- one phenotype type per phenotype), edited via single-select dropdowns.
-- The original text[] columns were a leftover from the prototype's
-- multi-value stringList widget. Collapse them to scalar text.
--
-- USING col[1] keeps any existing value (the arrays only ever held 0 or 1
-- element); an empty/NULL array becomes NULL.

--changeset rtaylor:phenotype-segregation-scalar
ALTER TABLE zirc.phenotype
    ALTER COLUMN p_segregation TYPE text USING p_segregation[1];

--changeset rtaylor:phenotype-type-scalar
ALTER TABLE zirc.phenotype
    ALTER COLUMN p_type TYPE text USING p_type[1];
