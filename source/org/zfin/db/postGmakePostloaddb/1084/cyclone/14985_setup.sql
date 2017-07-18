--liquibase formatted sql
--changeset pm:14985tempTable

CREATE TEMP TABLE tmp_person_country (
  tmp_pers_id VARCHAR(50),
  tmp_pers_country VARCHAR(50)
);
