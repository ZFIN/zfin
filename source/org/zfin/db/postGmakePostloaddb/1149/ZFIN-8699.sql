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

-- fixes for genotypes where we don't want to use superscript for deficiencies
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-010426-4') WHERE geno_zdb_id = 'ZDB-GENO-010426-4';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-010426-6') WHERE geno_zdb_id = 'ZDB-GENO-010426-6';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-050216-2') WHERE geno_zdb_id = 'ZDB-GENO-050216-2';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-050216-3') WHERE geno_zdb_id = 'ZDB-GENO-050216-3';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-060125-1') WHERE geno_zdb_id = 'ZDB-GENO-060125-1';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-070712-5') WHERE geno_zdb_id = 'ZDB-GENO-070712-5';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-080418-2') WHERE geno_zdb_id = 'ZDB-GENO-080418-2';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-080713-1') WHERE geno_zdb_id = 'ZDB-GENO-080713-1';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-080804-4') WHERE geno_zdb_id = 'ZDB-GENO-080804-4';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100325-4') WHERE geno_zdb_id = 'ZDB-GENO-100325-4';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100325-6') WHERE geno_zdb_id = 'ZDB-GENO-100325-6';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100601-2') WHERE geno_zdb_id = 'ZDB-GENO-100601-2';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-110105-3') WHERE geno_zdb_id = 'ZDB-GENO-110105-3';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-110105-4') WHERE geno_zdb_id = 'ZDB-GENO-110105-4';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-130402-3') WHERE geno_zdb_id = 'ZDB-GENO-130402-3';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-130402-5') WHERE geno_zdb_id = 'ZDB-GENO-130402-5';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-131018-4') WHERE geno_zdb_id = 'ZDB-GENO-131018-4';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-140825-1') WHERE geno_zdb_id = 'ZDB-GENO-140825-1';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-180208-11') WHERE geno_zdb_id = 'ZDB-GENO-180208-11';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-180208-15') WHERE geno_zdb_id = 'ZDB-GENO-180208-15';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-230615-11') WHERE geno_zdb_id = 'ZDB-GENO-230615-11';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-230615-8') WHERE geno_zdb_id = 'ZDB-GENO-230615-8';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-230615-6') WHERE geno_zdb_id = 'ZDB-GENO-230615-6';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-230615-9') WHERE geno_zdb_id = 'ZDB-GENO-230615-9';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-230615-7') WHERE geno_zdb_id = 'ZDB-GENO-230615-7';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-230615-10') WHERE geno_zdb_id = 'ZDB-GENO-230615-10';

-- fixes for genotypes after adding the rules for genotype_component_significance
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-001214-1') WHERE geno_zdb_id = 'ZDB-GENO-001214-1';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-010426-2') WHERE geno_zdb_id = 'ZDB-GENO-010426-2';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-070209-80') WHERE geno_zdb_id = 'ZDB-GENO-070209-80';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-070406-1') WHERE geno_zdb_id = 'ZDB-GENO-070406-1';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-070712-5') WHERE geno_zdb_id = 'ZDB-GENO-070712-5';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-080418-2') WHERE geno_zdb_id = 'ZDB-GENO-080418-2';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-080701-2') WHERE geno_zdb_id = 'ZDB-GENO-080701-2';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-080713-1') WHERE geno_zdb_id = 'ZDB-GENO-080713-1';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-080804-4') WHERE geno_zdb_id = 'ZDB-GENO-080804-4';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-080825-3') WHERE geno_zdb_id = 'ZDB-GENO-080825-3';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-091027-1') WHERE geno_zdb_id = 'ZDB-GENO-091027-1';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-091109-1') WHERE geno_zdb_id = 'ZDB-GENO-091109-1';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100325-3') WHERE geno_zdb_id = 'ZDB-GENO-100325-3';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100325-4') WHERE geno_zdb_id = 'ZDB-GENO-100325-4';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100325-5') WHERE geno_zdb_id = 'ZDB-GENO-100325-5';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100325-6') WHERE geno_zdb_id = 'ZDB-GENO-100325-6';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100506-7') WHERE geno_zdb_id = 'ZDB-GENO-100506-7';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100524-2') WHERE geno_zdb_id = 'ZDB-GENO-100524-2';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-100601-2') WHERE geno_zdb_id = 'ZDB-GENO-100601-2';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-101028-17') WHERE geno_zdb_id = 'ZDB-GENO-101028-17';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-130402-3') WHERE geno_zdb_id = 'ZDB-GENO-130402-3';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-130402-5') WHERE geno_zdb_id = 'ZDB-GENO-130402-5';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-140825-1') WHERE geno_zdb_id = 'ZDB-GENO-140825-1';
UPDATE genotype SET geno_display_name = get_genotype_display('ZDB-GENO-230117-2') WHERE geno_zdb_id = 'ZDB-GENO-230117-2';

