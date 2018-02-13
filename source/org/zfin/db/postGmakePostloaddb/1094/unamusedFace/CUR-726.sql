--liquibase formatted sql
--changeset xshao:CUR-726

delete from record_attribution where recattrib_pk_id = '317469';

delete from record_attribution where recattrib_pk_id = '53974685';

update marker_go_term_evidence 
                                set mrkrgoev_mrkr_zdb_id = 'ZDB-GENE-070117-2142'
                              where mrkrgoev_mrkr_zdb_id = 'ZDB-GENE-070117-437';

update marker_history 
                                set mhist_mrkr_zdb_id = 'ZDB-GENE-070117-2142'
                              where mhist_mrkr_zdb_id = 'ZDB-GENE-070117-437';

update marker_history_audit 
                                set mha_mrkr_zdb_id = 'ZDB-GENE-070117-2142'
                              where mha_mrkr_zdb_id = 'ZDB-GENE-070117-437';

update mutant_fast_search 
                                set mfs_mrkr_zdb_id = 'ZDB-GENE-070117-2142'
                              where mfs_mrkr_zdb_id = 'ZDB-GENE-070117-437';

update all_map_names 
                                set allmapnm_zdb_id = 'ZDB-GENE-070117-2142'
                              where allmapnm_zdb_id = 'ZDB-GENE-070117-437';

update data_alias 
                                set dalias_data_zdb_id = 'ZDB-GENE-070117-2142'
                              where dalias_data_zdb_id = 'ZDB-GENE-070117-437';

update db_link 
                                set dblink_linked_recid = 'ZDB-GENE-070117-2142'
                              where dblink_linked_recid = 'ZDB-GENE-070117-437';

update feature_marker_relationship 
                                set fmrel_mrkr_zdb_id = 'ZDB-GENE-070117-2142'
                              where fmrel_mrkr_zdb_id = 'ZDB-GENE-070117-437';



update record_attribution set recattrib_data_zdb_id = 'ZDB-GENE-070117-2142' where recattrib_data_zdb_id = 'ZDB-GENE-070117-437';

select get_id('DALIAS') as dalias_id, get_id('NOMEN') as nomen_id from single into temp tmp_ids;

insert into zdb_active_data select dalias_id from tmp_ids;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
                            select dalias_id, 'ZDB-GENE-070117-2142', 'unm_tf229', '1'
                              from tmp_ids;

insert into zdb_active_data select nomen_id from tmp_ids;

insert into marker_history (mhist_zdb_id, mhist_mrkr_zdb_id, mhist_event, mhist_reason, mhist_date, 
                                                          mhist_mrkr_name_on_mhist_date, mhist_mrkr_abbrev_on_mhist_date, mhist_comments,mhist_dalias_zdb_id)
                              select nomen_id, 'ZDB-GENE-070117-2142', 'merged', 'same marker', CURRENT, 
                                    'kefir', 'kef', 'none', dalias_id
                                from tmp_ids;

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-GENE-070117-437';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-GENE-070117-2142' where zrepld_new_zdb_id = 'ZDB-GENE-070117-437';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-070117-437';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-GENE-070117-437', 'ZDB-GENE-070117-2142');

delete from record_attribution where recattrib_pk_id = '1374011';

delete from record_attribution where recattrib_pk_id = '69814109';

delete from record_attribution where recattrib_pk_id = '70053154';

delete from record_attribution where recattrib_pk_id = '708127';

delete from record_attribution where recattrib_pk_id = '72846042';

delete from record_attribution where recattrib_pk_id = '72846043';

delete from feature_assay where featassay_feature_zdb_id = 'ZDB-ALT-980413-591';

update feature_assay set featassay_feature_zdb_id = 'ZDB-ALT-980413-591' where featassay_feature_zdb_id = 'ZDB-ALT-980203-1826'; 

delete from feature_marker_relationship where fmrel_zdb_id = 'ZDB-FMREL-070117-597';

update feature_marker_relationship 
                                set fmrel_ftr_zdb_id = 'ZDB-ALT-980413-591'
                              where fmrel_ftr_zdb_id = 'ZDB-ALT-980203-1826';

update genotype_feature 
                                set genofeat_feature_zdb_id = 'ZDB-ALT-980413-591'
                              where genofeat_feature_zdb_id = 'ZDB-ALT-980203-1826';

delete from int_data_source where ids_pk_id = '63911';

update int_data_source 
                                set ids_data_zdb_id = 'ZDB-ALT-980413-591'
                              where ids_data_zdb_id = 'ZDB-ALT-980203-1826';
                              
                              
update record_attribution set recattrib_data_zdb_id = 'ZDB-ALT-980413-591' where recattrib_data_zdb_id = 'ZDB-ALT-980203-1826';

select get_id('DALIAS') as dalias_id
  from single
 into temp tmp_alias_id;

insert into zdb_active_data select dalias_id from tmp_alias_id;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
                            select dalias_id, 'ZDB-ALT-980413-591', 'ztf229', '1'
                              from tmp_alias_id;

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-ALT-980203-1826';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-ALT-980413-591' where zrepld_new_zdb_id = 'ZDB-ALT-980203-1826';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ALT-980203-1826';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-ALT-980203-1826', 'ZDB-ALT-980413-591');

