--liquibase formatted sql
--changeset rtaylor:ZFIN-9718

-- Remove primary key (composite) from ortholog_external_reference
-- was created earlier with: ALTER TABLE ONLY public.ortholog_external_reference
--     ADD CONSTRAINT oef_ak_constraint PRIMARY KEY (oef_ortho_zdb_id, oef_accession_number, oef_fdbcont_zdb_id);
ALTER TABLE ortholog_external_reference
DROP CONSTRAINT IF EXISTS oef_ak_constraint;

-- Add a primary key column to ortholog_external_reference
ALTER TABLE ortholog_external_reference
ADD COLUMN IF NOT EXISTS oef_pk_id SERIAL PRIMARY KEY;

-- Add a unique constraint for the columns: "oef_ortho_zdb_id", "oef_accession_number", "oef_fdbcont_zdb_id"
ALTER TABLE ortholog_external_reference
ADD CONSTRAINT unique_ortholog_external_reference
UNIQUE (oef_ortho_zdb_id, oef_accession_number, oef_fdbcont_zdb_id);