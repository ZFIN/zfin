--liquibase formatted sql
--changeset rtaylor:ZFIN-8167

ALTER TABLE record_attribution ADD COLUMN recattrib_created_at TIMESTAMP;
ALTER TABLE record_attribution ALTER COLUMN recattrib_created_at SET DEFAULT now();

ALTER TABLE record_attribution ADD COLUMN recattrib_modified_at TIMESTAMP;
ALTER TABLE record_attribution ALTER COLUMN recattrib_modified_at SET DEFAULT now();

ALTER TABLE record_attribution ADD COLUMN recattrib_modified_count integer;
ALTER TABLE record_attribution ALTER COLUMN recattrib_modified_count SET DEFAULT 0;

