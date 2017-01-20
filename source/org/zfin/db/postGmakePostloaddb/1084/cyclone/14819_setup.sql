--liquibase formatted sql
--changeset pm:14819tempTable

CREATE TEMP TABLE tmp_old_ensdargs (
  tmp_dblink_id VARCHAR(50),
  tmp_gene_id VARCHAR(50)
);
