--liquibase formatted sql
--changeset pm:CUR-835

ALTER TABLE feature_assay DROP CONSTRAINT featassay_feature_zdb_id_fk;

ALTER TABLE feature_assay
ADD CONSTRAINT "featassay_feature_zdb_id_fk"
FOREIGN KEY (featassay_feature_zdb_id) REFERENCES feature ON DELETE CASCADE;

