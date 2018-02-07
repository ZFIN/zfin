--liquibase formatted sql
--changeset xshao:CUR-729

delete from record_attribution where recattrib_pk_id = '71427603';

delete from feature_marker_relationship where fmrel_zdb_id = 'ZDB-FMREL-161018-11';

update feature_history 
                                set fhist_ftr_zdb_id = 'ZDB-ALT-170815-7'
                              where fhist_ftr_zdb_id = 'ZDB-ALT-161018-11';

update feature_marker_relationship 
                                set fmrel_ftr_zdb_id = 'ZDB-ALT-170815-7'
                              where fmrel_ftr_zdb_id = 'ZDB-ALT-161018-11';

update genotype_feature 
                                set genofeat_feature_zdb_id = 'ZDB-ALT-170815-7'
                              where genofeat_feature_zdb_id = 'ZDB-ALT-161018-11';

update int_data_source 
                                set ids_data_zdb_id = 'ZDB-ALT-170815-7'
                              where ids_data_zdb_id = 'ZDB-ALT-161018-11';

update record_attribution set recattrib_data_zdb_id = 'ZDB-ALT-170815-7' where recattrib_data_zdb_id = 'ZDB-ALT-161018-11';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-ALT-161018-11';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-ALT-170815-7' where zrepld_new_zdb_id = 'ZDB-ALT-161018-11';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ALT-161018-11';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-ALT-161018-11', 'ZDB-ALT-170815-7');
