--liquibase formatted sql
--changeset pkalita:14538tempTable

CREATE TEMP TABLE tmp_feature_pubs (
  tmp_feature_id VARCHAR(50),
  tmp_pub_id VARCHAR(50)
);
