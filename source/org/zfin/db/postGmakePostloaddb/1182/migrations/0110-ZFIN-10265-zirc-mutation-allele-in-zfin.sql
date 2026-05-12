--liquibase formatted sql

-- Curators may submit a mutation either with an allele designation that
-- already exists in ZFIN (autocomplete from public.marker) or with a
-- new free-text designation. The PDF feedback adds an explicit checkbox
-- and a conditional-field swap. The new column records the curator's
-- answer; the allele designation column itself stays varchar so we can
-- accept either flavor.

--changeset rtaylor:zirc-mutation-allele-in-zfin
ALTER TABLE zirc.mutation
    ADD COLUMN m_allele_in_zfin BOOLEAN NOT NULL DEFAULT FALSE;
