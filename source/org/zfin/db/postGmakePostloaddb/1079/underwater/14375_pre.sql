--liquibase formatted sql
--changeset pkalita:createMutationDetailReferencesTempTable

CREATE TABLE tmp_md_file (
  feature_zdb_id VARCHAR(50),
  reference_zdb_id VARCHAR(50)
);
