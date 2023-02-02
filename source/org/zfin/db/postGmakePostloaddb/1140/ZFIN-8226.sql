--liquibase formatted sql
--changeset cmpich:ZFIN-8226

alter table monthly_curated_metric 
add column mcm_number_in_disease_bin integer;
