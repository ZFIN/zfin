--liquibase formatted sql
--changeset xshao:CUR-759

delete from all_name_ends where allnmend_allmapnm_serial_id = '1554945' and allnmend_name_end_lower = '(mpeg1:dendra2)';

delete from all_name_ends where allnmend_allmapnm_serial_id = '1554945' and allnmend_name_end_lower = 'eg1:dendra2)';

delete from all_name_ends where allnmend_allmapnm_serial_id = '1554945' and allnmend_name_end_lower = 'g(mpeg1:dendra2)';

delete from all_name_ends where allnmend_allmapnm_serial_id = '1554945' and allnmend_name_end_lower = 'g1:dendra2)';

delete from all_name_ends where allnmend_allmapnm_serial_id = '1554945' and allnmend_name_end_lower = 'mpeg1:dendra2)';

delete from all_name_ends where allnmend_allmapnm_serial_id = '1554945' and allnmend_name_end_lower = 'peg1:dendra2)';

delete from all_name_ends where allnmend_allmapnm_serial_id = '1554945' and allnmend_name_end_lower = 'tg(mpeg1:dendra2)';

delete from construct_marker_relationship where conmrkrrel_zdb_id = 'ZDB-CMREL-150506-4763';

delete from construct_marker_relationship where conmrkrrel_zdb_id = 'ZDB-CMREL-150506-4764';

delete from marker_relationship where mrel_zdb_id = 'ZDB-MREL-130205-1';

delete from marker_relationship where mrel_zdb_id = 'ZDB-MREL-130205-3';

update all_name_ends 
                                set allnmend_allmapnm_serial_id = '1355083'
                              where allnmend_allmapnm_serial_id = '1554945';

update construct_component 
                                set cc_construct_zdb_id = 'ZDB-TGCONSTRCT-150430-2'
                              where cc_construct_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

update construct_marker_relationship 
                                set conmrkrrel_construct_zdb_id = 'ZDB-TGCONSTRCT-150430-2'
                              where conmrkrrel_construct_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

update marker_history 
                                set mhist_mrkr_zdb_id = 'ZDB-TGCONSTRCT-150430-2'
                              where mhist_mrkr_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

update marker_history_audit 
                                set mha_mrkr_zdb_id = 'ZDB-TGCONSTRCT-150430-2'
                              where mha_mrkr_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

update marker_relationship 
                                set mrel_mrkr_1_zdb_id = 'ZDB-TGCONSTRCT-150430-2'
                              where mrel_mrkr_1_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

delete from all_map_names where allmapnm_name = 'Tg(mpeg1:Dendra2)';

update all_map_names 
                                set allmapnm_zdb_id = 'ZDB-TGCONSTRCT-150430-2'
                              where allmapnm_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

update feature_marker_relationship 
                                set fmrel_mrkr_zdb_id = 'ZDB-TGCONSTRCT-150430-2'
                              where fmrel_mrkr_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

update record_attribution set recattrib_data_zdb_id = 'ZDB-TGCONSTRCT-150430-2' where recattrib_data_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-TGCONSTRCT-150430-2' where zrepld_new_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-TGCONSTRCT-130205-1';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-TGCONSTRCT-130205-1', 'ZDB-TGCONSTRCT-150430-2');

delete from record_attribution where recattrib_pk_id = '72843958';

delete from feature_assay where featassay_pk_id = '54673';

delete from feature_marker_relationship where fmrel_zdb_id = 'ZDB-FMREL-150430-1';

update feature_assay
                                set featassay_feature_zdb_id = 'ZDB-ALT-130205-1'
                              where featassay_feature_zdb_id = 'ZDB-ALT-150430-1';

update feature_history
                                set fhist_ftr_zdb_id = 'ZDB-ALT-130205-1'
                              where fhist_ftr_zdb_id = 'ZDB-ALT-150430-1';

update feature_marker_relationship
                                set fmrel_ftr_zdb_id = 'ZDB-ALT-130205-1'
                              where fmrel_ftr_zdb_id = 'ZDB-ALT-150430-1';

update genotype_feature
                                set genofeat_feature_zdb_id = 'ZDB-ALT-130205-1'
                              where genofeat_feature_zdb_id = 'ZDB-ALT-150430-1';

delete from int_data_source where ids_pk_id = '78260';

update int_data_source
                                set ids_data_zdb_id = 'ZDB-ALT-130205-1'
                              where ids_data_zdb_id = 'ZDB-ALT-150430-1';

update record_attribution set recattrib_data_zdb_id = 'ZDB-ALT-130205-1' where recattrib_data_zdb_id = 'ZDB-ALT-150430-1';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-ALT-150430-1';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-ALT-130205-1' where zrepld_new_zdb_id = 'ZDB-ALT-150430-1';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ALT-150430-1';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-ALT-150430-1', 'ZDB-ALT-130205-1');

create temp table tmp_alias_id (
dalias_id varchar(50)
);

insert into tmp_alias_id
select get_id('DALIAS') from single;


insert into zdb_active_data select dalias_id from tmp_alias_id;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
                            select dalias_id, 'ZDB-ALT-130205-1', 'uwm23Tg', '1'
                              from tmp_alias_id;

