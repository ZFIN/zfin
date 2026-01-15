--liquibase formatted sql
--changeset rtaylor:ZFIN-10009.sql

-- merging feature ZDB-ALT-120206-1 (zf256Tg) into ZDB-ALT-170711-6 (ihb175Tg)

-- ### Tables and actions to take on rows that contain old ZDB IDs:
-- action	table	column1
-- delete	feature	ZDB-ALT-120206-1
-- delete	feature_assay	ZDB-ALT-120206-1
-- update	feature_group_member	6656
-- update	feature_history	ZDB-FHIST-120206-1
-- delete	feature_marker_relationship	ZDB-FMREL-251106-1
-- update	feature_tracking	9515
-- update	fish_components	250834048
-- update	genotype_feature	ZDB-GENOFEAT-120206-6
-- delete	int_data_source	53445
-- delete	int_data_supplier	ZDB-ALT-120206-1
-- keep	pub_correspondence_sent_email	7750
-- delete	record_attribution	7667
-- update	record_attribution	1339311
-- update	record_attribution	72838659
-- keep	updates	ZDB-PERS-100329-1
-- keep	updates	ZDB-PERS-100329-1
-- keep	updates	ZDB-PERS-100329-1
-- keep	updates	ZDB-PERS-100329-1
-- keep	updates	ZDB-PERS-100329-1
-- keep	updates	ZDB-PERS-100329-1
-- keep	updates	ZDB-PERS-100329-1
-- delete	zdb_active_data	ZDB-ALT-120206-1
-- insert	zdb_replaced_data
-- insert	alias



DELETE FROM feature_assay WHERE featassay_feature_zdb_id = 'ZDB-ALT-120206-1';
UPDATE feature_group_member SET fgm_member_id = 'ZDB-ALT-170711-6' WHERE fgm_member_id = 'ZDB-ALT-120206-1';
UPDATE feature_history SET fhist_ftr_zdb_id = 'ZDB-ALT-170711-6' WHERE fhist_ftr_zdb_id = 'ZDB-ALT-120206-1';
DELETE FROM feature_marker_relationship WHERE fmrel_ftr_zdb_id = 'ZDB-ALT-120206-1';
UPDATE feature_tracking SET ft_feature_zdb_id = 'ZDB-ALT-170711-6' WHERE ft_feature_zdb_id = 'ZDB-ALT-120206-1';

update fish_components set fc_fish_name = 'ihb175Tg', fc_affector_zdb_id = 'ZDB-ALT-170711-6' where fc_affector_zdb_id = 'ZDB-ALT-120206-1';
update genotype_feature set genofeat_feature_zdb_id = 'ZDB-ALT-170711-6' where genofeat_feature_zdb_id = 'ZDB-ALT-120206-1';

DELETE FROM int_data_source WHERE ids_data_zdb_id = 'ZDB-ALT-120206-1';
DELETE FROM int_data_supplier WHERE idsup_data_zdb_id = 'ZDB-ALT-120206-1';

DELETE FROM record_attribution WHERE recattrib_data_zdb_id = 'ZDB-ALT-120206-1' AND recattrib_pk_id = 7667;
UPDATE record_attribution SET recattrib_data_zdb_id = 'ZDB-ALT-170711-6' where recattrib_data_zdb_id = 'ZDB-ALT-120206-1' and recattrib_pk_id in (1339311, 72838659);

DELETE FROM zdb_active_data WHERE zactvd_zdb_id = 'ZDB-ALT-120206-1';
INSERT INTO zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id, zrepld_old_name) VALUES ('ZDB-ALT-120206-1', 'ZDB-ALT-170711-6', 'zf256Tg');

-- Normally we would add an alias here, but it has been manually added already:
-- INSERT INTO data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id) VALUES
--    (get_id_and_insert_active_data('DALIAS'), 'ZDB-ALT-170711-6', 'zf256Tg', '1');

