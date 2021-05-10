--liquibase formatted sql
--changeset patrick:INF-3348.sql

ALTER TABLE zdb_object_type
  DROP COLUMN zobjtype_app_page;
