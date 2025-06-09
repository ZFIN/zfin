--liquibase formatted sql
--changeset rtaylor:ZFIN-9700

ALTER TABLE feature_genomic_mutation_detail ADD COLUMN fgmd_modified_at TIMESTAMP;

CREATE OR REPLACE FUNCTION feature_genomic_mutation_detail_sync_modified_at()
  RETURNS trigger AS '
BEGIN
   NEW.fgmd_modified_at := NOW();
RETURN NEW;
END;
' LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS feature_genomic_mutation_detail_sync_modified_at_trigger on feature_genomic_mutation_detail;

CREATE TRIGGER
    feature_genomic_mutation_detail_sync_modified_at_trigger
    BEFORE UPDATE ON
    feature_genomic_mutation_detail
    FOR EACH ROW EXECUTE PROCEDURE
    feature_genomic_mutation_detail_sync_modified_at();
