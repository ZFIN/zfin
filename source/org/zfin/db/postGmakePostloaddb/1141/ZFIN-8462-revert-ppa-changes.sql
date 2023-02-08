--liquibase formatted sql
--changeset rtaylor:ZFIN-8462-revert-ppa-changes.sql

-- add a created_at column to the pubmed_publication_author table
ALTER TABLE pubmed_publication_author ADD COLUMN ppa_created_at TIMESTAMP;
ALTER TABLE pubmed_publication_author ALTER COLUMN ppa_created_at SET DEFAULT now();

-- delete all changes to the ppa table since Jan 31, 2023 when we last deployed the jenkins job
DELETE FROM pubmed_publication_author WHERE ppa_pk_id >= 536655 AND ppa_created_at IS NULL;
