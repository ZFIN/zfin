--liquibase formatted sql
--changeset xshao:CUR-729

delete from record_attribution where recattrib_pk_id = '65690741';

delete from feature_marker_relationship where fmrel_zdb_id = 'ZDB-FMREL-171212-10';

update feature_history 
                                set fhist_ftr_zdb_id = 'ZDB-ALT-161018-11'
                              where fhist_ftr_zdb_id = 'ZDB-ALT-170815-7';

update feature_marker_relationship 
                                set fmrel_ftr_zdb_id = 'ZDB-ALT-161018-11'
                              where fmrel_ftr_zdb_id = 'ZDB-ALT-170815-7';

update genotype_feature 
                                set genofeat_feature_zdb_id = 'ZDB-ALT-161018-11'
                              where genofeat_feature_zdb_id = 'ZDB-ALT-170815-7';

delete from int_data_source where ids_data_zdb_id = 'ZDB-ALT-170815-7'; 

delete from record_attribution where recattrib_pk_id = '65690741';

update record_attribution set recattrib_data_zdb_id = 'ZDB-ALT-161018-11' where recattrib_data_zdb_id = 'ZDB-ALT-170815-7';

select get_id('DALIAS') as dalias_id
  from single
 into temp tmp_id;

insert into zdb_active_data select dalias_id from tmp_id;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
                            select dalias_id, 'ZDB-ALT-161018-11', 'zf740Tg', '1'
                              from tmp_id;

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-ALT-170815-7';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-ALT-161018-11' where zrepld_new_zdb_id = 'ZDB-ALT-170815-7';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ALT-170815-7';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-ALT-170815-7', 'ZDB-ALT-161018-11');
