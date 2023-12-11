--liquibase formatted sql
--changeset cmpich:ZFIN-8877.sql

ALTER TABLE zdb_replaced_data ADD COLUMN zrepld_date_created TIMESTAMP;
ALTER TABLE zdb_replaced_data ALTER COLUMN zrepld_date_created SET DEFAULT now();
