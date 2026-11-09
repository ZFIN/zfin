--liquibase formatted sql

--changeset rtaylor:ZFIN-10579-publication-file-source-and-person
-- Record how a publication file was added and, for manual uploads, by whom.
-- pf_source: CURATOR_UPLOAD, PMC_LOADER or ZEBRASHARE_SUBMISSION. NULL = unknown (rows that
-- predate this column; the data does not reliably say whether they were loaded or uploaded).
-- pf_person_zdb_id: the uploading person; NULL for automated loads and for unknown.
ALTER TABLE publication_file ADD COLUMN pf_source text;
ALTER TABLE publication_file ADD COLUMN pf_person_zdb_id text;

ALTER TABLE publication_file
  ADD CONSTRAINT pf_source_check
  CHECK (pf_source IS NULL OR pf_source IN ('CURATOR_UPLOAD', 'PMC_LOADER', 'ZEBRASHARE_SUBMISSION'));

ALTER TABLE publication_file
  ADD CONSTRAINT pf_person_zdb_id_fk
  FOREIGN KEY (pf_person_zdb_id) REFERENCES person (zdb_id) ON DELETE SET NULL;

CREATE INDEX publication_file_person_fk_index ON publication_file (pf_person_zdb_id);
