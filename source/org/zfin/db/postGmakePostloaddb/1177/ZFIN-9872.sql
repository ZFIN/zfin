--liquibase formatted sql
--changeset cmpich:ZFIN-9872

-- Merging duplicate antibodies with same AB_2261274 registry ID
-- Keep: ZDB-ATB-120322-1 (Ab1-ctsk)
-- Merge into it: ZDB-ATB-180801-1 (Ab2-ctsk)

-- Update expression_experiment2 references
UPDATE expression_experiment2 SET xpatex_atb_zdb_id = 'ZDB-ATB-120322-1' WHERE xpatex_atb_zdb_id = 'ZDB-ATB-180801-1';

-- Update marker_relationship (delete duplicates first, then update remaining)
DELETE FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-180801-1'
  AND (mrel_mrkr_1_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_1_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-120322-1'
  );
UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-ATB-120322-1' WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-180801-1';
DELETE FROM marker_relationship
WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-180801-1'
  AND (mrel_mrkr_2_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_2_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-120322-1'
  );
UPDATE marker_relationship SET mrel_mrkr_1_zdb_id = 'ZDB-ATB-120322-1' WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-180801-1';

-- Update marker_history
UPDATE marker_history SET mhist_mrkr_zdb_id = 'ZDB-ATB-120322-1' WHERE mhist_mrkr_zdb_id = 'ZDB-ATB-180801-1';

-- Update data_alias (delete duplicates first)
DELETE FROM data_alias
WHERE dalias_data_zdb_id = 'ZDB-ATB-180801-1'
  AND (dalias_alias, dalias_group_id) IN (
    SELECT dalias_alias, dalias_group_id FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-120322-1'
  );
UPDATE data_alias SET dalias_data_zdb_id = 'ZDB-ATB-120322-1' WHERE dalias_data_zdb_id = 'ZDB-ATB-180801-1';

-- Update db_link (delete duplicates first to avoid unique constraint violation)
DELETE FROM db_link
WHERE dblink_linked_recid = 'ZDB-ATB-180801-1'
  AND (dblink_acc_num, dblink_fdbcont_zdb_id) IN (
    SELECT dblink_acc_num, dblink_fdbcont_zdb_id FROM db_link WHERE dblink_linked_recid = 'ZDB-ATB-120322-1'
  );
UPDATE db_link SET dblink_linked_recid = 'ZDB-ATB-120322-1' WHERE dblink_linked_recid = 'ZDB-ATB-180801-1';

-- Update record_attribution (move attributions to target, delete duplicates)
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id = 'ZDB-ATB-180801-1'
  AND recattrib_source_zdb_id IN (
    SELECT recattrib_source_zdb_id FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ATB-120322-1'
  );
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ATB-120322-1' WHERE recattrib_data_zdb_id = 'ZDB-ATB-180801-1';

-- Update int_data_source
DELETE FROM int_data_source
WHERE ids_data_zdb_id = 'ZDB-ATB-180801-1'
  AND ids_source_zdb_id IN (
    SELECT ids_source_zdb_id FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ATB-120322-1'
  );
UPDATE int_data_source SET ids_data_zdb_id = 'ZDB-ATB-120322-1' WHERE ids_data_zdb_id = 'ZDB-ATB-180801-1';

-- Update int_data_supplier
DELETE FROM int_data_supplier
WHERE idsup_data_zdb_id = 'ZDB-ATB-180801-1'
  AND idsup_supplier_zdb_id IN (
    SELECT idsup_supplier_zdb_id FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ATB-120322-1'
  );
UPDATE int_data_supplier SET idsup_data_zdb_id = 'ZDB-ATB-120322-1' WHERE idsup_data_zdb_id = 'ZDB-ATB-180801-1';

-- Update updates table
UPDATE updates SET rec_id = 'ZDB-ATB-120322-1' WHERE rec_id = 'ZDB-ATB-180801-1';

-- Update external_note
UPDATE external_note SET extnote_data_zdb_id = 'ZDB-ATB-120322-1' WHERE extnote_data_zdb_id = 'ZDB-ATB-180801-1';

-- Update ortholog
UPDATE ortholog SET ortho_other_species_ncbi_gene_id = 'ZDB-ATB-120322-1' WHERE ortho_other_species_ncbi_gene_id = 'ZDB-ATB-180801-1';

-- Update phenotype_observation_generated (references marker)
UPDATE phenotype_observation_generated SET psg_mrkr_zdb_id = 'ZDB-ATB-120322-1' WHERE psg_mrkr_zdb_id = 'ZDB-ATB-180801-1';

-- Update publication_expression_display (references marker)
UPDATE ui.publication_expression_display SET ped_antibody_zdb_id = 'ZDB-ATB-120322-1' WHERE ped_antibody_zdb_id = 'ZDB-ATB-180801-1';

-- Delete the antibody record (this will cascade to antibody table)
DELETE FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-180801-1';
DELETE FROM marker WHERE mrkr_zdb_id = 'ZDB-ATB-180801-1';

-- Remove from active data and add to replaced data
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ATB-180801-1';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
VALUES ('ZDB-ATB-180801-1', 'ZDB-ATB-120322-1', 'Ab2-ctsk');

-- Add alias for the merged antibody name (only if not already exists)
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
SELECT get_id_and_insert_active_data('DALIAS'), 'ZDB-ATB-120322-1', 'Ab2-ctsk', '1'
WHERE NOT EXISTS (SELECT 1 FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-120322-1' AND dalias_alias = 'Ab2-ctsk');

-- Log the merge in updates table
INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when)
VALUES ('ZDB-PERS-030520-1', 'ZDB-ATB-120322-1', 'zdb_id', 'ZDB-ATB-120322-1', 'ZDB-ATB-180801-1',
        'Merged duplicate antibody ZDB-ATB-180801-1 (Ab2-ctsk) - both have same AB_2261274 registry ID',
        'Pich, Christian', now());

-- ============================================================================
-- Merging duplicate antibodies with same AB_2300502 registry ID
-- Keep: ZDB-ATB-090130-1 (Ab1-ptk2.1)
-- Merge into it: ZDB-ATB-171206-3 (Ab4-ptk2a)
-- ============================================================================

-- Update expression_experiment2 references
UPDATE expression_experiment2 SET xpatex_atb_zdb_id = 'ZDB-ATB-090130-1' WHERE xpatex_atb_zdb_id = 'ZDB-ATB-171206-3';

-- Update marker_relationship (delete duplicates first, then update remaining)
DELETE FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-171206-3'
  AND (mrel_mrkr_1_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_1_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-090130-1'
  );
UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-ATB-090130-1' WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-171206-3';
DELETE FROM marker_relationship
WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-171206-3'
  AND (mrel_mrkr_2_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_2_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-090130-1'
  );
UPDATE marker_relationship SET mrel_mrkr_1_zdb_id = 'ZDB-ATB-090130-1' WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-171206-3';

-- Update marker_history
UPDATE marker_history SET mhist_mrkr_zdb_id = 'ZDB-ATB-090130-1' WHERE mhist_mrkr_zdb_id = 'ZDB-ATB-171206-3';

-- Update data_alias (delete duplicates first)
DELETE FROM data_alias
WHERE dalias_data_zdb_id = 'ZDB-ATB-171206-3'
  AND (dalias_alias, dalias_group_id) IN (
    SELECT dalias_alias, dalias_group_id FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-090130-1'
  );
UPDATE data_alias SET dalias_data_zdb_id = 'ZDB-ATB-090130-1' WHERE dalias_data_zdb_id = 'ZDB-ATB-171206-3';

-- Update db_link (delete duplicates first to avoid unique constraint violation)
DELETE FROM db_link
WHERE dblink_linked_recid = 'ZDB-ATB-171206-3'
  AND (dblink_acc_num, dblink_fdbcont_zdb_id) IN (
    SELECT dblink_acc_num, dblink_fdbcont_zdb_id FROM db_link WHERE dblink_linked_recid = 'ZDB-ATB-090130-1'
  );
UPDATE db_link SET dblink_linked_recid = 'ZDB-ATB-090130-1' WHERE dblink_linked_recid = 'ZDB-ATB-171206-3';

-- Update record_attribution (move attributions to target, delete duplicates)
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id = 'ZDB-ATB-171206-3'
  AND recattrib_source_zdb_id IN (
    SELECT recattrib_source_zdb_id FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ATB-090130-1'
  );
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ATB-090130-1' WHERE recattrib_data_zdb_id = 'ZDB-ATB-171206-3';

-- Update int_data_source
DELETE FROM int_data_source
WHERE ids_data_zdb_id = 'ZDB-ATB-171206-3'
  AND ids_source_zdb_id IN (
    SELECT ids_source_zdb_id FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ATB-090130-1'
  );
UPDATE int_data_source SET ids_data_zdb_id = 'ZDB-ATB-090130-1' WHERE ids_data_zdb_id = 'ZDB-ATB-171206-3';

-- Update int_data_supplier
DELETE FROM int_data_supplier
WHERE idsup_data_zdb_id = 'ZDB-ATB-171206-3'
  AND idsup_supplier_zdb_id IN (
    SELECT idsup_supplier_zdb_id FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ATB-090130-1'
  );
UPDATE int_data_supplier SET idsup_data_zdb_id = 'ZDB-ATB-090130-1' WHERE idsup_data_zdb_id = 'ZDB-ATB-171206-3';

-- Update updates table
UPDATE updates SET rec_id = 'ZDB-ATB-090130-1' WHERE rec_id = 'ZDB-ATB-171206-3';

-- Update external_note
UPDATE external_note SET extnote_data_zdb_id = 'ZDB-ATB-090130-1' WHERE extnote_data_zdb_id = 'ZDB-ATB-171206-3';

-- Update ortholog
UPDATE ortholog SET ortho_other_species_ncbi_gene_id = 'ZDB-ATB-090130-1' WHERE ortho_other_species_ncbi_gene_id = 'ZDB-ATB-171206-3';

-- Update phenotype_observation_generated (references marker)
UPDATE phenotype_observation_generated SET psg_mrkr_zdb_id = 'ZDB-ATB-090130-1' WHERE psg_mrkr_zdb_id = 'ZDB-ATB-171206-3';

-- Update publication_expression_display (references marker)
UPDATE ui.publication_expression_display SET ped_antibody_zdb_id = 'ZDB-ATB-090130-1' WHERE ped_antibody_zdb_id = 'ZDB-ATB-171206-3';

-- Delete the antibody record (this will cascade to antibody table)
DELETE FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-171206-3';
DELETE FROM marker WHERE mrkr_zdb_id = 'ZDB-ATB-171206-3';

-- Remove from active data and add to replaced data
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ATB-171206-3';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
VALUES ('ZDB-ATB-171206-3', 'ZDB-ATB-090130-1', 'Ab4-ptk2a');

-- Add alias for the merged antibody name (only if not already exists)
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
SELECT get_id_and_insert_active_data('DALIAS'), 'ZDB-ATB-090130-1', 'Ab4-ptk2a', '1'
WHERE NOT EXISTS (SELECT 1 FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-090130-1' AND dalias_alias = 'Ab4-ptk2a');

-- Log the merge in updates table
INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when)
VALUES ('ZDB-PERS-030520-1', 'ZDB-ATB-090130-1', 'zdb_id', 'ZDB-ATB-090130-1', 'ZDB-ATB-171206-3',
        'Merged duplicate antibody ZDB-ATB-171206-3 (Ab4-ptk2a) - both have same AB_2300502 registry ID',
        'Pich, Christian', now());

-- ============================================================================
-- Merging duplicate antibodies with same AB_259852 registry ID
-- Keep: ZDB-ATB-081124-4 (Ab1-gcg)
-- Merge into it: ZDB-ATB-200224-1 (Ab3-gcg)
-- ============================================================================

-- Update expression_experiment2 references
UPDATE expression_experiment2 SET xpatex_atb_zdb_id = 'ZDB-ATB-081124-4' WHERE xpatex_atb_zdb_id = 'ZDB-ATB-200224-1';

-- Update marker_relationship (delete duplicates first, then update remaining)
DELETE FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-200224-1'
  AND (mrel_mrkr_1_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_1_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-081124-4'
  );
UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-ATB-081124-4' WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-200224-1';
DELETE FROM marker_relationship
WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-200224-1'
  AND (mrel_mrkr_2_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_2_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-081124-4'
  );
UPDATE marker_relationship SET mrel_mrkr_1_zdb_id = 'ZDB-ATB-081124-4' WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-200224-1';

-- Update marker_history
UPDATE marker_history SET mhist_mrkr_zdb_id = 'ZDB-ATB-081124-4' WHERE mhist_mrkr_zdb_id = 'ZDB-ATB-200224-1';

-- Update data_alias (delete duplicates first)
DELETE FROM data_alias
WHERE dalias_data_zdb_id = 'ZDB-ATB-200224-1'
  AND (dalias_alias, dalias_group_id) IN (
    SELECT dalias_alias, dalias_group_id FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-081124-4'
  );
UPDATE data_alias SET dalias_data_zdb_id = 'ZDB-ATB-081124-4' WHERE dalias_data_zdb_id = 'ZDB-ATB-200224-1';

-- Update db_link (delete duplicates first to avoid unique constraint violation)
DELETE FROM db_link
WHERE dblink_linked_recid = 'ZDB-ATB-200224-1'
  AND (dblink_acc_num, dblink_fdbcont_zdb_id) IN (
    SELECT dblink_acc_num, dblink_fdbcont_zdb_id FROM db_link WHERE dblink_linked_recid = 'ZDB-ATB-081124-4'
  );
UPDATE db_link SET dblink_linked_recid = 'ZDB-ATB-081124-4' WHERE dblink_linked_recid = 'ZDB-ATB-200224-1';

-- Update record_attribution (move attributions to target, delete duplicates)
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id = 'ZDB-ATB-200224-1'
  AND recattrib_source_zdb_id IN (
    SELECT recattrib_source_zdb_id FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ATB-081124-4'
  );
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ATB-081124-4' WHERE recattrib_data_zdb_id = 'ZDB-ATB-200224-1';

-- Update int_data_source
DELETE FROM int_data_source
WHERE ids_data_zdb_id = 'ZDB-ATB-200224-1'
  AND ids_source_zdb_id IN (
    SELECT ids_source_zdb_id FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ATB-081124-4'
  );
UPDATE int_data_source SET ids_data_zdb_id = 'ZDB-ATB-081124-4' WHERE ids_data_zdb_id = 'ZDB-ATB-200224-1';

-- Update int_data_supplier
DELETE FROM int_data_supplier
WHERE idsup_data_zdb_id = 'ZDB-ATB-200224-1'
  AND idsup_supplier_zdb_id IN (
    SELECT idsup_supplier_zdb_id FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ATB-081124-4'
  );
UPDATE int_data_supplier SET idsup_data_zdb_id = 'ZDB-ATB-081124-4' WHERE idsup_data_zdb_id = 'ZDB-ATB-200224-1';

-- Update updates table
UPDATE updates SET rec_id = 'ZDB-ATB-081124-4' WHERE rec_id = 'ZDB-ATB-200224-1';

-- Update external_note
UPDATE external_note SET extnote_data_zdb_id = 'ZDB-ATB-081124-4' WHERE extnote_data_zdb_id = 'ZDB-ATB-200224-1';

-- Update ortholog
UPDATE ortholog SET ortho_other_species_ncbi_gene_id = 'ZDB-ATB-081124-4' WHERE ortho_other_species_ncbi_gene_id = 'ZDB-ATB-200224-1';

-- Update phenotype_observation_generated (references marker)
UPDATE phenotype_observation_generated SET psg_mrkr_zdb_id = 'ZDB-ATB-081124-4' WHERE psg_mrkr_zdb_id = 'ZDB-ATB-200224-1';

-- Update publication_expression_display (references marker)
UPDATE ui.publication_expression_display SET ped_antibody_zdb_id = 'ZDB-ATB-081124-4' WHERE ped_antibody_zdb_id = 'ZDB-ATB-200224-1';


-- Copy immunogen organism from source to target
UPDATE antibody SET atb_immun_organism = (SELECT atb_immun_organism FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-200224-1')
WHERE atb_zdb_id = 'ZDB-ATB-081124-4' AND atb_immun_organism IS NULL;

-- Delete the antibody record (this will cascade to antibody table)
DELETE FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-200224-1';
DELETE FROM marker WHERE mrkr_zdb_id = 'ZDB-ATB-200224-1';

-- Remove from active data and add to replaced data
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ATB-200224-1';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
VALUES ('ZDB-ATB-200224-1', 'ZDB-ATB-081124-4', 'Ab3-gcg');

-- Add alias for the merged antibody name (only if not already exists)
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
SELECT get_id_and_insert_active_data('DALIAS'), 'ZDB-ATB-081124-4', 'Ab3-gcg', '1'
WHERE NOT EXISTS (SELECT 1 FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-081124-4' AND dalias_alias = 'Ab3-gcg');

-- Log the merge in updates table
INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when)
VALUES ('ZDB-PERS-030520-1', 'ZDB-ATB-081124-4', 'zdb_id', 'ZDB-ATB-081124-4', 'ZDB-ATB-200224-1',
        'Merged duplicate antibody ZDB-ATB-200224-1 (Ab3-gcg) - both have same AB_259852 registry ID',
        'Pich, Christian', now());

-- ============================================================================
-- Merging duplicate antibodies with same AB_399888 registry ID
-- Keep: ZDB-ATB-181001-1 (Ab2-opa1)
-- Merge into it: ZDB-ATB-181210-4 (Ab3-opa1)
-- ============================================================================

-- Update expression_experiment2 references
UPDATE expression_experiment2 SET xpatex_atb_zdb_id = 'ZDB-ATB-181001-1' WHERE xpatex_atb_zdb_id = 'ZDB-ATB-181210-4';

-- Update marker_relationship (delete duplicates first, then update remaining)
DELETE FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-181210-4'
  AND (mrel_mrkr_1_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_1_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-181001-1'
  );
UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-ATB-181001-1' WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-181210-4';
DELETE FROM marker_relationship
WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-181210-4'
  AND (mrel_mrkr_2_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_2_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-181001-1'
  );
UPDATE marker_relationship SET mrel_mrkr_1_zdb_id = 'ZDB-ATB-181001-1' WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-181210-4';

-- Update marker_history
UPDATE marker_history SET mhist_mrkr_zdb_id = 'ZDB-ATB-181001-1' WHERE mhist_mrkr_zdb_id = 'ZDB-ATB-181210-4';

-- Update data_alias (delete duplicates first)
DELETE FROM data_alias
WHERE dalias_data_zdb_id = 'ZDB-ATB-181210-4'
  AND (dalias_alias, dalias_group_id) IN (
    SELECT dalias_alias, dalias_group_id FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-181001-1'
  );
UPDATE data_alias SET dalias_data_zdb_id = 'ZDB-ATB-181001-1' WHERE dalias_data_zdb_id = 'ZDB-ATB-181210-4';

-- Update db_link (delete duplicates first to avoid unique constraint violation)
DELETE FROM db_link
WHERE dblink_linked_recid = 'ZDB-ATB-181210-4'
  AND (dblink_acc_num, dblink_fdbcont_zdb_id) IN (
    SELECT dblink_acc_num, dblink_fdbcont_zdb_id FROM db_link WHERE dblink_linked_recid = 'ZDB-ATB-181001-1'
  );
UPDATE db_link SET dblink_linked_recid = 'ZDB-ATB-181001-1' WHERE dblink_linked_recid = 'ZDB-ATB-181210-4';

-- Update record_attribution (move attributions to target, delete duplicates)
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id = 'ZDB-ATB-181210-4'
  AND recattrib_source_zdb_id IN (
    SELECT recattrib_source_zdb_id FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ATB-181001-1'
  );
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ATB-181001-1' WHERE recattrib_data_zdb_id = 'ZDB-ATB-181210-4';

-- Update int_data_source
DELETE FROM int_data_source
WHERE ids_data_zdb_id = 'ZDB-ATB-181210-4'
  AND ids_source_zdb_id IN (
    SELECT ids_source_zdb_id FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ATB-181001-1'
  );
UPDATE int_data_source SET ids_data_zdb_id = 'ZDB-ATB-181001-1' WHERE ids_data_zdb_id = 'ZDB-ATB-181210-4';

-- Update int_data_supplier
DELETE FROM int_data_supplier
WHERE idsup_data_zdb_id = 'ZDB-ATB-181210-4'
  AND idsup_supplier_zdb_id IN (
    SELECT idsup_supplier_zdb_id FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ATB-181001-1'
  );
UPDATE int_data_supplier SET idsup_data_zdb_id = 'ZDB-ATB-181001-1' WHERE idsup_data_zdb_id = 'ZDB-ATB-181210-4';

-- Update updates table
UPDATE updates SET rec_id = 'ZDB-ATB-181001-1' WHERE rec_id = 'ZDB-ATB-181210-4';

-- Update external_note
UPDATE external_note SET extnote_data_zdb_id = 'ZDB-ATB-181001-1' WHERE extnote_data_zdb_id = 'ZDB-ATB-181210-4';

-- Update ortholog
UPDATE ortholog SET ortho_other_species_ncbi_gene_id = 'ZDB-ATB-181001-1' WHERE ortho_other_species_ncbi_gene_id = 'ZDB-ATB-181210-4';

-- Update phenotype_observation_generated (references marker)
UPDATE phenotype_observation_generated SET psg_mrkr_zdb_id = 'ZDB-ATB-181001-1' WHERE psg_mrkr_zdb_id = 'ZDB-ATB-181210-4';

-- Update publication_expression_display (references marker)
UPDATE ui.publication_expression_display SET ped_antibody_zdb_id = 'ZDB-ATB-181001-1' WHERE ped_antibody_zdb_id = 'ZDB-ATB-181210-4';

-- Delete the antibody record (this will cascade to antibody table)
DELETE FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-181210-4';
DELETE FROM marker WHERE mrkr_zdb_id = 'ZDB-ATB-181210-4';

-- Remove from active data and add to replaced data
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ATB-181210-4';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
VALUES ('ZDB-ATB-181210-4', 'ZDB-ATB-181001-1', 'Ab3-opa1');

-- Add alias for the merged antibody name (only if not already exists)
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
SELECT get_id_and_insert_active_data('DALIAS'), 'ZDB-ATB-181001-1', 'Ab3-opa1', '1'
WHERE NOT EXISTS (SELECT 1 FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-181001-1' AND dalias_alias = 'Ab3-opa1');

-- Log the merge in updates table
INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when)
VALUES ('ZDB-PERS-030520-1', 'ZDB-ATB-181001-1', 'zdb_id', 'ZDB-ATB-181001-1', 'ZDB-ATB-181210-4',
        'Merged duplicate antibody ZDB-ATB-181210-4 (Ab3-opa1) - both have same AB_399888 registry ID',
        'Pich, Christian', now());

-- ============================================================================
-- Merging duplicate antibodies with same AB_400326 registry ID
-- Keep: ZDB-ATB-090304-2 (Ab5-brdu)
-- Merge into it: ZDB-ATB-090316-1 (Ab1-brdu/idu)
-- ============================================================================

-- Update expression_experiment2 references
UPDATE expression_experiment2 SET xpatex_atb_zdb_id = 'ZDB-ATB-090304-2' WHERE xpatex_atb_zdb_id = 'ZDB-ATB-090316-1';

-- Update marker_relationship (delete duplicates first, then update remaining)
DELETE FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-090316-1'
  AND (mrel_mrkr_1_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_1_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-090304-2'
  );
UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-ATB-090304-2' WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-090316-1';
DELETE FROM marker_relationship
WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-090316-1'
  AND (mrel_mrkr_2_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_2_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-090304-2'
  );
UPDATE marker_relationship SET mrel_mrkr_1_zdb_id = 'ZDB-ATB-090304-2' WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-090316-1';

-- Update marker_history
UPDATE marker_history SET mhist_mrkr_zdb_id = 'ZDB-ATB-090304-2' WHERE mhist_mrkr_zdb_id = 'ZDB-ATB-090316-1';

-- Update data_alias (delete duplicates first)
DELETE FROM data_alias
WHERE dalias_data_zdb_id = 'ZDB-ATB-090316-1'
  AND (dalias_alias, dalias_group_id) IN (
    SELECT dalias_alias, dalias_group_id FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-090304-2'
  );
UPDATE data_alias SET dalias_data_zdb_id = 'ZDB-ATB-090304-2' WHERE dalias_data_zdb_id = 'ZDB-ATB-090316-1';

-- Update db_link (delete duplicates first to avoid unique constraint violation)
DELETE FROM db_link
WHERE dblink_linked_recid = 'ZDB-ATB-090316-1'
  AND (dblink_acc_num, dblink_fdbcont_zdb_id) IN (
    SELECT dblink_acc_num, dblink_fdbcont_zdb_id FROM db_link WHERE dblink_linked_recid = 'ZDB-ATB-090304-2'
  );
UPDATE db_link SET dblink_linked_recid = 'ZDB-ATB-090304-2' WHERE dblink_linked_recid = 'ZDB-ATB-090316-1';

-- Update record_attribution (move attributions to target, delete duplicates)
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id = 'ZDB-ATB-090316-1'
  AND recattrib_source_zdb_id IN (
    SELECT recattrib_source_zdb_id FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ATB-090304-2'
  );
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ATB-090304-2' WHERE recattrib_data_zdb_id = 'ZDB-ATB-090316-1';

-- Update int_data_source
DELETE FROM int_data_source
WHERE ids_data_zdb_id = 'ZDB-ATB-090316-1'
  AND ids_source_zdb_id IN (
    SELECT ids_source_zdb_id FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ATB-090304-2'
  );
UPDATE int_data_source SET ids_data_zdb_id = 'ZDB-ATB-090304-2' WHERE ids_data_zdb_id = 'ZDB-ATB-090316-1';

-- Update int_data_supplier
DELETE FROM int_data_supplier
WHERE idsup_data_zdb_id = 'ZDB-ATB-090316-1'
  AND idsup_supplier_zdb_id IN (
    SELECT idsup_supplier_zdb_id FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ATB-090304-2'
  );
UPDATE int_data_supplier SET idsup_data_zdb_id = 'ZDB-ATB-090304-2' WHERE idsup_data_zdb_id = 'ZDB-ATB-090316-1';

-- Update updates table
UPDATE updates SET rec_id = 'ZDB-ATB-090304-2' WHERE rec_id = 'ZDB-ATB-090316-1';

-- Update external_note
UPDATE external_note SET extnote_data_zdb_id = 'ZDB-ATB-090304-2' WHERE extnote_data_zdb_id = 'ZDB-ATB-090316-1';

-- Update ortholog
UPDATE ortholog SET ortho_other_species_ncbi_gene_id = 'ZDB-ATB-090304-2' WHERE ortho_other_species_ncbi_gene_id = 'ZDB-ATB-090316-1';

-- Update phenotype_observation_generated (references marker)
UPDATE phenotype_observation_generated SET psg_mrkr_zdb_id = 'ZDB-ATB-090304-2' WHERE psg_mrkr_zdb_id = 'ZDB-ATB-090316-1';

-- Update publication_expression_display (references marker)
UPDATE ui.publication_expression_display SET ped_antibody_zdb_id = 'ZDB-ATB-090304-2' WHERE ped_antibody_zdb_id = 'ZDB-ATB-090316-1';

-- Delete the antibody record (this will cascade to antibody table)
DELETE FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-090316-1';
DELETE FROM marker WHERE mrkr_zdb_id = 'ZDB-ATB-090316-1';

-- Remove from active data and add to replaced data
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ATB-090316-1';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
VALUES ('ZDB-ATB-090316-1', 'ZDB-ATB-090304-2', 'Ab1-brdu/idu');

-- Add alias for the merged antibody name (only if not already exists)
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
SELECT get_id_and_insert_active_data('DALIAS'), 'ZDB-ATB-090304-2', 'Ab1-brdu/idu', '1'
WHERE NOT EXISTS (SELECT 1 FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-090304-2' AND dalias_alias = 'Ab1-brdu/idu');

-- Log the merge in updates table
INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when)
VALUES ('ZDB-PERS-030520-1', 'ZDB-ATB-090304-2', 'zdb_id', 'ZDB-ATB-090304-2', 'ZDB-ATB-090316-1',
        'Merged duplicate antibody ZDB-ATB-090316-1 (Ab1-brdu/idu) - both have same AB_400326 registry ID',
        'Pich, Christian', now());

-- ============================================================================
-- Merging duplicate antibodies with same AB_627224 registry ID
-- Keep: ZDB-ATB-180327-2 (Ab1-cdk1)
-- Merge into it: ZDB-ATB-130808-2 (Ab3-cdc2)
-- ============================================================================

-- Update expression_experiment2 references
UPDATE expression_experiment2 SET xpatex_atb_zdb_id = 'ZDB-ATB-180327-2' WHERE xpatex_atb_zdb_id = 'ZDB-ATB-130808-2';

-- Update marker_relationship (delete duplicates first, then update remaining)
DELETE FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-130808-2'
  AND (mrel_mrkr_1_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_1_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-180327-2'
  );
UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-ATB-180327-2' WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-130808-2';
DELETE FROM marker_relationship
WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-130808-2'
  AND (mrel_mrkr_2_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_2_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-180327-2'
  );
UPDATE marker_relationship SET mrel_mrkr_1_zdb_id = 'ZDB-ATB-180327-2' WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-130808-2';

-- Update marker_history
UPDATE marker_history SET mhist_mrkr_zdb_id = 'ZDB-ATB-180327-2' WHERE mhist_mrkr_zdb_id = 'ZDB-ATB-130808-2';

-- Update data_alias (delete duplicates first)
DELETE FROM data_alias
WHERE dalias_data_zdb_id = 'ZDB-ATB-130808-2'
  AND (dalias_alias, dalias_group_id) IN (
    SELECT dalias_alias, dalias_group_id FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-180327-2'
  );
UPDATE data_alias SET dalias_data_zdb_id = 'ZDB-ATB-180327-2' WHERE dalias_data_zdb_id = 'ZDB-ATB-130808-2';

-- Update db_link (delete duplicates first to avoid unique constraint violation)
DELETE FROM db_link
WHERE dblink_linked_recid = 'ZDB-ATB-130808-2'
  AND (dblink_acc_num, dblink_fdbcont_zdb_id) IN (
    SELECT dblink_acc_num, dblink_fdbcont_zdb_id FROM db_link WHERE dblink_linked_recid = 'ZDB-ATB-180327-2'
  );
UPDATE db_link SET dblink_linked_recid = 'ZDB-ATB-180327-2' WHERE dblink_linked_recid = 'ZDB-ATB-130808-2';

-- Update record_attribution (move attributions to target, delete duplicates)
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id = 'ZDB-ATB-130808-2'
  AND recattrib_source_zdb_id IN (
    SELECT recattrib_source_zdb_id FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ATB-180327-2'
  );
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ATB-180327-2' WHERE recattrib_data_zdb_id = 'ZDB-ATB-130808-2';

-- Update int_data_source
DELETE FROM int_data_source
WHERE ids_data_zdb_id = 'ZDB-ATB-130808-2'
  AND ids_source_zdb_id IN (
    SELECT ids_source_zdb_id FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ATB-180327-2'
  );
UPDATE int_data_source SET ids_data_zdb_id = 'ZDB-ATB-180327-2' WHERE ids_data_zdb_id = 'ZDB-ATB-130808-2';

-- Update int_data_supplier
DELETE FROM int_data_supplier
WHERE idsup_data_zdb_id = 'ZDB-ATB-130808-2'
  AND idsup_supplier_zdb_id IN (
    SELECT idsup_supplier_zdb_id FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ATB-180327-2'
  );
UPDATE int_data_supplier SET idsup_data_zdb_id = 'ZDB-ATB-180327-2' WHERE idsup_data_zdb_id = 'ZDB-ATB-130808-2';

-- Update updates table
UPDATE updates SET rec_id = 'ZDB-ATB-180327-2' WHERE rec_id = 'ZDB-ATB-130808-2';

-- Update external_note
UPDATE external_note SET extnote_data_zdb_id = 'ZDB-ATB-180327-2' WHERE extnote_data_zdb_id = 'ZDB-ATB-130808-2';

-- Update ortholog
UPDATE ortholog SET ortho_other_species_ncbi_gene_id = 'ZDB-ATB-180327-2' WHERE ortho_other_species_ncbi_gene_id = 'ZDB-ATB-130808-2';

-- Update phenotype_observation_generated (references marker)
UPDATE phenotype_observation_generated SET psg_mrkr_zdb_id = 'ZDB-ATB-180327-2' WHERE psg_mrkr_zdb_id = 'ZDB-ATB-130808-2';

-- Update publication_expression_display (references marker)
UPDATE ui.publication_expression_display SET ped_antibody_zdb_id = 'ZDB-ATB-180327-2' WHERE ped_antibody_zdb_id = 'ZDB-ATB-130808-2';

-- Copy immunogen organism from source to target
UPDATE antibody SET atb_immun_organism = (SELECT atb_immun_organism FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-130808-2')
WHERE atb_zdb_id = 'ZDB-ATB-180327-2' AND atb_immun_organism IS NULL;

-- Delete the antibody record (this will cascade to antibody table)
DELETE FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-130808-2';
DELETE FROM marker WHERE mrkr_zdb_id = 'ZDB-ATB-130808-2';

-- Remove from active data and add to replaced data
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ATB-130808-2';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
VALUES ('ZDB-ATB-130808-2', 'ZDB-ATB-180327-2', 'Ab3-cdc2');

-- Add alias for the merged antibody name (only if not already exists)
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
SELECT get_id_and_insert_active_data('DALIAS'), 'ZDB-ATB-180327-2', 'Ab3-cdc2', '1'
WHERE NOT EXISTS (SELECT 1 FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-180327-2' AND dalias_alias = 'Ab3-cdc2');

-- Log the merge in updates table
INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when)
VALUES ('ZDB-PERS-030520-1', 'ZDB-ATB-180327-2', 'zdb_id', 'ZDB-ATB-180327-2', 'ZDB-ATB-130808-2',
        'Merged duplicate antibody ZDB-ATB-130808-2 (Ab3-cdc2) - both have same AB_627224 registry ID',
        'Pich, Christian', now());

-- ============================================================================
-- Merging duplicate antibodies with same AB_10664825 registry ID
-- Keep: ZDB-ATB-151005-6 (Ab1-gnb1)
-- Merge into it: ZDB-ATB-170816-1 (Ab3-gnb1)
-- ============================================================================

-- Update expression_experiment2 references
UPDATE expression_experiment2 SET xpatex_atb_zdb_id = 'ZDB-ATB-151005-6' WHERE xpatex_atb_zdb_id = 'ZDB-ATB-170816-1';

-- Update marker_relationship (delete duplicates first, then update remaining)
DELETE FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-170816-1'
  AND (mrel_mrkr_1_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_1_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-151005-6'
  );
UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-ATB-151005-6' WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-170816-1';
DELETE FROM marker_relationship
WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-170816-1'
  AND (mrel_mrkr_2_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_2_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-151005-6'
  );
UPDATE marker_relationship SET mrel_mrkr_1_zdb_id = 'ZDB-ATB-151005-6' WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-170816-1';

-- Update marker_history
UPDATE marker_history SET mhist_mrkr_zdb_id = 'ZDB-ATB-151005-6' WHERE mhist_mrkr_zdb_id = 'ZDB-ATB-170816-1';

-- Update data_alias (delete duplicates first)
DELETE FROM data_alias
WHERE dalias_data_zdb_id = 'ZDB-ATB-170816-1'
  AND (dalias_alias, dalias_group_id) IN (
    SELECT dalias_alias, dalias_group_id FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-151005-6'
  );
UPDATE data_alias SET dalias_data_zdb_id = 'ZDB-ATB-151005-6' WHERE dalias_data_zdb_id = 'ZDB-ATB-170816-1';

-- Update db_link (delete duplicates first to avoid unique constraint violation)
DELETE FROM db_link
WHERE dblink_linked_recid = 'ZDB-ATB-170816-1'
  AND (dblink_acc_num, dblink_fdbcont_zdb_id) IN (
    SELECT dblink_acc_num, dblink_fdbcont_zdb_id FROM db_link WHERE dblink_linked_recid = 'ZDB-ATB-151005-6'
  );
UPDATE db_link SET dblink_linked_recid = 'ZDB-ATB-151005-6' WHERE dblink_linked_recid = 'ZDB-ATB-170816-1';

-- Update record_attribution (move attributions to target, delete duplicates)
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id = 'ZDB-ATB-170816-1'
  AND recattrib_source_zdb_id IN (
    SELECT recattrib_source_zdb_id FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ATB-151005-6'
  );
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ATB-151005-6' WHERE recattrib_data_zdb_id = 'ZDB-ATB-170816-1';

-- Update int_data_source
DELETE FROM int_data_source
WHERE ids_data_zdb_id = 'ZDB-ATB-170816-1'
  AND ids_source_zdb_id IN (
    SELECT ids_source_zdb_id FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ATB-151005-6'
  );
UPDATE int_data_source SET ids_data_zdb_id = 'ZDB-ATB-151005-6' WHERE ids_data_zdb_id = 'ZDB-ATB-170816-1';

-- Update int_data_supplier
DELETE FROM int_data_supplier
WHERE idsup_data_zdb_id = 'ZDB-ATB-170816-1'
  AND idsup_supplier_zdb_id IN (
    SELECT idsup_supplier_zdb_id FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ATB-151005-6'
  );
UPDATE int_data_supplier SET idsup_data_zdb_id = 'ZDB-ATB-151005-6' WHERE idsup_data_zdb_id = 'ZDB-ATB-170816-1';

-- Update updates table
UPDATE updates SET rec_id = 'ZDB-ATB-151005-6' WHERE rec_id = 'ZDB-ATB-170816-1';

-- Update external_note
UPDATE external_note SET extnote_data_zdb_id = 'ZDB-ATB-151005-6' WHERE extnote_data_zdb_id = 'ZDB-ATB-170816-1';

-- Update ortholog
UPDATE ortholog SET ortho_other_species_ncbi_gene_id = 'ZDB-ATB-151005-6' WHERE ortho_other_species_ncbi_gene_id = 'ZDB-ATB-170816-1';

-- Update phenotype_observation_generated (references marker)
UPDATE phenotype_observation_generated SET psg_mrkr_zdb_id = 'ZDB-ATB-151005-6' WHERE psg_mrkr_zdb_id = 'ZDB-ATB-170816-1';

-- Update publication_expression_display (references marker)
UPDATE ui.publication_expression_display SET ped_antibody_zdb_id = 'ZDB-ATB-151005-6' WHERE ped_antibody_zdb_id = 'ZDB-ATB-170816-1';

-- Delete the antibody record (this will cascade to antibody table)
DELETE FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-170816-1';
DELETE FROM marker WHERE mrkr_zdb_id = 'ZDB-ATB-170816-1';

-- Remove from active data and add to replaced data
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ATB-170816-1';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
VALUES ('ZDB-ATB-170816-1', 'ZDB-ATB-151005-6', 'Ab3-gnb1');

-- Add alias for the merged antibody name (only if not already exists)
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
SELECT get_id_and_insert_active_data('DALIAS'), 'ZDB-ATB-151005-6', 'Ab3-gnb1', '1'
WHERE NOT EXISTS (SELECT 1 FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-151005-6' AND dalias_alias = 'Ab3-gnb1');

-- Log the merge in updates table
INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when)
VALUES ('ZDB-PERS-030520-1', 'ZDB-ATB-151005-6', 'zdb_id', 'ZDB-ATB-151005-6', 'ZDB-ATB-170816-1',
        'Merged duplicate antibody ZDB-ATB-170816-1 (Ab3-gnb1) - both have same AB_10664825 registry ID',
        'Pich, Christian', now());

-- ============================================================================
-- Merging duplicate antibodies with same AB_2116445 registry ID
-- Keep: ZDB-ATB-160518-1 (Ab1-havcr1)
-- Merge into it: ZDB-ATB-181030-2 (Ab1-havcr)
-- ============================================================================

-- Update expression_experiment2 references
UPDATE expression_experiment2 SET xpatex_atb_zdb_id = 'ZDB-ATB-160518-1' WHERE xpatex_atb_zdb_id = 'ZDB-ATB-181030-2';

-- Update marker_relationship (delete duplicates first, then update remaining)
DELETE FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-181030-2'
  AND (mrel_mrkr_1_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_1_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-ATB-160518-1' WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-181030-2';
DELETE FROM marker_relationship
WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-181030-2'
  AND (mrel_mrkr_2_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_2_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE marker_relationship SET mrel_mrkr_1_zdb_id = 'ZDB-ATB-160518-1' WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-181030-2';

-- Update marker_history
UPDATE marker_history SET mhist_mrkr_zdb_id = 'ZDB-ATB-160518-1' WHERE mhist_mrkr_zdb_id = 'ZDB-ATB-181030-2';

-- Update data_alias (delete duplicates first)
DELETE FROM data_alias
WHERE dalias_data_zdb_id = 'ZDB-ATB-181030-2'
  AND (dalias_alias, dalias_group_id) IN (
    SELECT dalias_alias, dalias_group_id FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE data_alias SET dalias_data_zdb_id = 'ZDB-ATB-160518-1' WHERE dalias_data_zdb_id = 'ZDB-ATB-181030-2';

-- Update db_link (delete duplicates first to avoid unique constraint violation)
DELETE FROM db_link
WHERE dblink_linked_recid = 'ZDB-ATB-181030-2'
  AND (dblink_acc_num, dblink_fdbcont_zdb_id) IN (
    SELECT dblink_acc_num, dblink_fdbcont_zdb_id FROM db_link WHERE dblink_linked_recid = 'ZDB-ATB-160518-1'
  );
UPDATE db_link SET dblink_linked_recid = 'ZDB-ATB-160518-1' WHERE dblink_linked_recid = 'ZDB-ATB-181030-2';

-- Update record_attribution (move attributions to target, delete duplicates)
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id = 'ZDB-ATB-181030-2'
  AND recattrib_source_zdb_id IN (
    SELECT recattrib_source_zdb_id FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ATB-160518-1' WHERE recattrib_data_zdb_id = 'ZDB-ATB-181030-2';

-- Update int_data_source
DELETE FROM int_data_source
WHERE ids_data_zdb_id = 'ZDB-ATB-181030-2'
  AND ids_source_zdb_id IN (
    SELECT ids_source_zdb_id FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE int_data_source SET ids_data_zdb_id = 'ZDB-ATB-160518-1' WHERE ids_data_zdb_id = 'ZDB-ATB-181030-2';

-- Update int_data_supplier
DELETE FROM int_data_supplier
WHERE idsup_data_zdb_id = 'ZDB-ATB-181030-2'
  AND idsup_supplier_zdb_id IN (
    SELECT idsup_supplier_zdb_id FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE int_data_supplier SET idsup_data_zdb_id = 'ZDB-ATB-160518-1' WHERE idsup_data_zdb_id = 'ZDB-ATB-181030-2';

-- Update updates table
UPDATE updates SET rec_id = 'ZDB-ATB-160518-1' WHERE rec_id = 'ZDB-ATB-181030-2';

-- Update external_note
UPDATE external_note SET extnote_data_zdb_id = 'ZDB-ATB-160518-1' WHERE extnote_data_zdb_id = 'ZDB-ATB-181030-2';

-- Update ortholog
UPDATE ortholog SET ortho_other_species_ncbi_gene_id = 'ZDB-ATB-160518-1' WHERE ortho_other_species_ncbi_gene_id = 'ZDB-ATB-181030-2';

-- Update phenotype_observation_generated (references marker)
UPDATE phenotype_observation_generated SET psg_mrkr_zdb_id = 'ZDB-ATB-160518-1' WHERE psg_mrkr_zdb_id = 'ZDB-ATB-181030-2';

-- Update publication_expression_display (references marker)
UPDATE ui.publication_expression_display SET ped_antibody_zdb_id = 'ZDB-ATB-160518-1' WHERE ped_antibody_zdb_id = 'ZDB-ATB-181030-2';

-- Delete the antibody record (this will cascade to antibody table)
DELETE FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-181030-2';
DELETE FROM marker WHERE mrkr_zdb_id = 'ZDB-ATB-181030-2';

-- Remove from active data and add to replaced data
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ATB-181030-2';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
VALUES ('ZDB-ATB-181030-2', 'ZDB-ATB-160518-1', 'Ab1-havcr');

-- Add alias for the merged antibody name (only if not already exists)
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
SELECT get_id_and_insert_active_data('DALIAS'), 'ZDB-ATB-160518-1', 'Ab1-havcr', '1'
WHERE NOT EXISTS (SELECT 1 FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-160518-1' AND dalias_alias = 'Ab1-havcr');

-- Log the merge in updates table
INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when)
VALUES ('ZDB-PERS-030520-1', 'ZDB-ATB-160518-1', 'zdb_id', 'ZDB-ATB-160518-1', 'ZDB-ATB-181030-2',
        'Merged duplicate antibody ZDB-ATB-181030-2 (Ab1-havcr) - both have same AB_2116445 registry ID',
        'Pich, Christian', now());

-- ============================================================================
-- Merging duplicate antibodies with same AB_2116445 registry ID
-- Keep: ZDB-ATB-160518-1 (Ab1-havcr1)
-- Merge into it: ZDB-ATB-190925-3 (Ab2-havcr1)
-- ============================================================================

-- Update expression_experiment2 references
UPDATE expression_experiment2 SET xpatex_atb_zdb_id = 'ZDB-ATB-160518-1' WHERE xpatex_atb_zdb_id = 'ZDB-ATB-190925-3';

-- Update marker_relationship (delete duplicates first, then update remaining)
DELETE FROM marker_relationship
WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-190925-3'
  AND (mrel_mrkr_1_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_1_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE marker_relationship SET mrel_mrkr_2_zdb_id = 'ZDB-ATB-160518-1' WHERE mrel_mrkr_2_zdb_id = 'ZDB-ATB-190925-3';
DELETE FROM marker_relationship
WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-190925-3'
  AND (mrel_mrkr_2_zdb_id, mrel_type) IN (
    SELECT mrel_mrkr_2_zdb_id, mrel_type FROM marker_relationship WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE marker_relationship SET mrel_mrkr_1_zdb_id = 'ZDB-ATB-160518-1' WHERE mrel_mrkr_1_zdb_id = 'ZDB-ATB-190925-3';

-- Update marker_history
UPDATE marker_history SET mhist_mrkr_zdb_id = 'ZDB-ATB-160518-1' WHERE mhist_mrkr_zdb_id = 'ZDB-ATB-190925-3';

-- Update data_alias (delete duplicates first)
DELETE FROM data_alias
WHERE dalias_data_zdb_id = 'ZDB-ATB-190925-3'
  AND (dalias_alias, dalias_group_id) IN (
    SELECT dalias_alias, dalias_group_id FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE data_alias SET dalias_data_zdb_id = 'ZDB-ATB-160518-1' WHERE dalias_data_zdb_id = 'ZDB-ATB-190925-3';

-- Update db_link (delete duplicates first to avoid unique constraint violation)
DELETE FROM db_link
WHERE dblink_linked_recid = 'ZDB-ATB-190925-3'
  AND (dblink_acc_num, dblink_fdbcont_zdb_id) IN (
    SELECT dblink_acc_num, dblink_fdbcont_zdb_id FROM db_link WHERE dblink_linked_recid = 'ZDB-ATB-160518-1'
  );
UPDATE db_link SET dblink_linked_recid = 'ZDB-ATB-160518-1' WHERE dblink_linked_recid = 'ZDB-ATB-190925-3';

-- Update record_attribution (move attributions to target, delete duplicates)
DELETE FROM record_attribution
WHERE recattrib_data_zdb_id = 'ZDB-ATB-190925-3'
  AND recattrib_source_zdb_id IN (
    SELECT recattrib_source_zdb_id FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ATB-160518-1' WHERE recattrib_data_zdb_id = 'ZDB-ATB-190925-3';

-- Update int_data_source
DELETE FROM int_data_source
WHERE ids_data_zdb_id = 'ZDB-ATB-190925-3'
  AND ids_source_zdb_id IN (
    SELECT ids_source_zdb_id FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE int_data_source SET ids_data_zdb_id = 'ZDB-ATB-160518-1' WHERE ids_data_zdb_id = 'ZDB-ATB-190925-3';

-- Update int_data_supplier
DELETE FROM int_data_supplier
WHERE idsup_data_zdb_id = 'ZDB-ATB-190925-3'
  AND idsup_supplier_zdb_id IN (
    SELECT idsup_supplier_zdb_id FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ATB-160518-1'
  );
UPDATE int_data_supplier SET idsup_data_zdb_id = 'ZDB-ATB-160518-1' WHERE idsup_data_zdb_id = 'ZDB-ATB-190925-3';

-- Update updates table
UPDATE updates SET rec_id = 'ZDB-ATB-160518-1' WHERE rec_id = 'ZDB-ATB-190925-3';

-- Update external_note
UPDATE external_note SET extnote_data_zdb_id = 'ZDB-ATB-160518-1' WHERE extnote_data_zdb_id = 'ZDB-ATB-190925-3';

-- Update ortholog
UPDATE ortholog SET ortho_other_species_ncbi_gene_id = 'ZDB-ATB-160518-1' WHERE ortho_other_species_ncbi_gene_id = 'ZDB-ATB-190925-3';

-- Update phenotype_observation_generated (references marker)
UPDATE phenotype_observation_generated SET psg_mrkr_zdb_id = 'ZDB-ATB-160518-1' WHERE psg_mrkr_zdb_id = 'ZDB-ATB-190925-3';

-- Update publication_expression_display (references marker)
UPDATE ui.publication_expression_display SET ped_antibody_zdb_id = 'ZDB-ATB-160518-1' WHERE ped_antibody_zdb_id = 'ZDB-ATB-190925-3';

-- Copy external note from source to target
INSERT INTO external_note (extnote_zdb_id, extnote_data_zdb_id, extnote_note, extnote_source_zdb_id)
SELECT get_id_and_insert_active_data('EXTNOTE'), 'ZDB-ATB-160518-1', extnote_note, extnote_source_zdb_id
FROM external_note WHERE extnote_data_zdb_id = 'ZDB-ATB-190925-3';

-- Delete the old external note
DELETE FROM external_note WHERE extnote_data_zdb_id = 'ZDB-ATB-190925-3';

-- Move curator note to target antibody
UPDATE data_note SET dnote_data_zdb_id = 'ZDB-ATB-160518-1' WHERE dnote_data_zdb_id = 'ZDB-ATB-190925-3';

-- Delete the antibody record (this will cascade to antibody table)
DELETE FROM antibody WHERE atb_zdb_id = 'ZDB-ATB-190925-3';
DELETE FROM marker WHERE mrkr_zdb_id = 'ZDB-ATB-190925-3';

-- Remove from active data and add to replaced data
DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ATB-190925-3';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name)
VALUES ('ZDB-ATB-190925-3', 'ZDB-ATB-160518-1', 'Ab2-havcr1');

-- Add alias for the merged antibody name (only if not already exists)
INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
SELECT get_id_and_insert_active_data('DALIAS'), 'ZDB-ATB-160518-1', 'Ab2-havcr1', '1'
WHERE NOT EXISTS (SELECT 1 FROM data_alias WHERE dalias_data_zdb_id = 'ZDB-ATB-160518-1' AND dalias_alias = 'Ab2-havcr1');

-- Log the merge in updates table
INSERT INTO updates (submitter_id, rec_id, field_name, new_value, old_value, comments, submitter_name, upd_when)
VALUES ('ZDB-PERS-030520-1', 'ZDB-ATB-160518-1', 'zdb_id', 'ZDB-ATB-160518-1', 'ZDB-ATB-190925-3',
        'Merged duplicate antibody ZDB-ATB-190925-3 (Ab2-havcr1) - both have same AB_2116445 registry ID',
        'Pich, Christian', now());
