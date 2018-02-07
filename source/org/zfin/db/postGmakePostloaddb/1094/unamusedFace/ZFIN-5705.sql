--liquibase formatted sql
--changeset xshao:ZFIN-5705

delete from record_attribution where recattrib_pk_id = '706401';

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

update int_data_source
                                set ids_data_zdb_id = 'ZDB-ALT-170424-1'
                              where ids_data_zdb_id = 'ZDB-ALT-140123-14';

delete from record_attribution where recattrib_pk_id = '706401';

update record_attribution set recattrib_data_zdb_id = 'ZDB-ALT-170424-1' where recattrib_data_zdb_id = 'ZDB-ALT-140123-14';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-ALT-140123-14';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-ALT-170424-1' where zrepld_new_zdb_id = 'ZDB-ALT-140123-14';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ALT-140123-14';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-ALT-140123-14', 'ZDB-ALT-170424-1');


