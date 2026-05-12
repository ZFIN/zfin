--liquibase formatted sql

-- Widen zirc.mutation_publication.mp_publication from VARCHAR(255) to
-- VARCHAR(1000). The 255-char ceiling truncates fuller citations (full
-- author lists + journal info) that curators paste while a Pub ID has
-- not yet been minted.
--
--changeset rtaylor:zirc-mutation-publication-widen
ALTER TABLE zirc.mutation_publication ALTER COLUMN mp_publication TYPE VARCHAR(1000);
