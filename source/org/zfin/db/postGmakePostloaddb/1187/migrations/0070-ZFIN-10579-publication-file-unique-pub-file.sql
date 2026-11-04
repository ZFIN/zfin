--liquibase formatted sql

--changeset rtaylor:ZFIN-10579-publication-file-unique-pub-file
-- A publication records each stored file (pf_file_name is the path under the loadup directory) once.
-- Runs after 0060, which deletes the existing duplicates. A file has one type; recording it twice (the
-- PMC loader once did so every day) is never meaningful.
-- The loaders use ON CONFLICT DO NOTHING on this constraint, and a curator upload of a file that already
-- exists asks whether to replace it.
ALTER TABLE publication_file
  ADD CONSTRAINT publication_file_pub_file_name_uq UNIQUE (pf_pub_zdb_id, pf_file_name);
--rollback ALTER TABLE publication_file DROP CONSTRAINT publication_file_pub_file_name_uq;
