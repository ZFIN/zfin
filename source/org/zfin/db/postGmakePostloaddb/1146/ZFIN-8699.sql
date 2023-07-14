--liquibase formatted sql
--changeset rtaylor:ZFIN-8699.sql

-- use the same significance for 'mutation involves' as 'is allele of'
INSERT INTO genotype_component_significance (gcs_mrkr_type, gcs_ftr_type, gcs_fmrel_type, gcs_significance) (
    SELECT
        gcs_mrkr_type,
        gcs_ftr_type,
        'mutation involves' AS gcs_fmrel_type,
        gcs_significance
    FROM
        genotype_component_significance
    WHERE
        gcs_fmrel_type = 'is allele of');
