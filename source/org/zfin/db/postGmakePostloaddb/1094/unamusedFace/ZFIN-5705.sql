--liquibase formatted sql
--changeset xshao:ZFIN-5705

delete from record_attribution where recattrib_pk_id = '706401';

delete from feature_assay where featassay_feature_zdb_id = 'ZDB-ALT-170424-1';

update feature_assay set featassay_feature_zdb_id = 'ZDB-ALT-170424-1' where featassay_feature_zdb_id = 'ZDB-ALT-140123-14';

update feature_history
                                set fhist_ftr_zdb_id = 'ZDB-ALT-170424-1'
                              where fhist_ftr_zdb_id = 'ZDB-ALT-140123-14';

update genotype_feature
                                set genofeat_feature_zdb_id = 'ZDB-ALT-170424-1'
                              where genofeat_feature_zdb_id = 'ZDB-ALT-140123-14';

delete from data_alias where dalias_zdb_id = 'ZDB-DALIAS-140123-30';

update data_alias
                                set dalias_data_zdb_id = 'ZDB-ALT-170424-1'
                              where dalias_data_zdb_id = 'ZDB-ALT-140123-14';

select get_id('DALIAS') as dalias_id
  from single
 into temp tmp_aliasid;

insert into zdb_active_data select dalias_id from tmp_aliasid;

insert into data_alias (dalias_zdb_id, dalias_data_zdb_id, dalias_alias, dalias_group_id)
                            select dalias_id, 'ZDB-ALT-170424-1', 'sm1120', '1'
                              from tmp_aliasid;


delete from int_data_source
                              where ids_data_zdb_id = 'ZDB-ALT-140123-14';

delete from record_attribution where recattrib_pk_id = '706401';

update record_attribution set recattrib_data_zdb_id = 'ZDB-ALT-170424-1' where recattrib_data_zdb_id = 'ZDB-ALT-140123-14';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-ALT-140123-14';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-ALT-170424-1' where zrepld_new_zdb_id = 'ZDB-ALT-140123-14';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ALT-140123-14';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-ALT-140123-14', 'ZDB-ALT-170424-1');


