--liquibase formatted sql
--changeset cmpich:ZFIN-8971.sql

alter table monthly_curated_metric
    add mcm_number_in_xenograft_bin integer NOT NULL DEFAULT 0;

alter table monthly_curated_metric
    add mcm_number_in_drug_bin integer NOT NULL DEFAULT 0;

alter table monthly_curated_metric
    add mcm_number_in_environment_tox_bin integer NOT NULL DEFAULT 0;

alter table monthly_curated_metric
    add mcm_number_in_natural_product_bin integer NOT NULL DEFAULT 0;

alter table monthly_curated_metric
    add mcm_number_in_nanomaterial_bin integer NOT NULL DEFAULT 0;

