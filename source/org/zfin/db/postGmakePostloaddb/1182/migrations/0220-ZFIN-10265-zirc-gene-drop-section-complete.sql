--liquibase formatted sql

-- Drop zirc.gene.g_section_complete. The original "Section complete?"
-- per-gene curator flag was redundant with the natural collapse signal
-- (a gene having been picked) and added cognitive overhead without any
-- downstream query branching on it.
--
--changeset rtaylor:zirc-gene-drop-section-complete
ALTER TABLE zirc.gene DROP COLUMN IF EXISTS g_section_complete;
