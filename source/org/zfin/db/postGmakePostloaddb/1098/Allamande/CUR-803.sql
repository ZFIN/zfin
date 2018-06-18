--liquibase formatted sql
--changeset xshao:CUR-803

delete from record_attribution where recattrib_pk_id = '74678627';

update fish
                                set fish_genotype_zdb_id = 'ZDB-GENO-180522-1'
                              where fish_genotype_zdb_id = 'ZDB-GENO-071024-1';

update record_attribution set recattrib_data_zdb_id = 'ZDB-GENO-180522-1' where recattrib_data_zdb_id = 'ZDB-GENO-071024-1';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-GENO-071024-1';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-GENO-180522-1' where zrepld_new_zdb_id = 'ZDB-GENO-071024-1';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENO-071024-1';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-GENO-071024-1', 'ZDB-GENO-180522-1');

delete from record_attribution where recattrib_pk_id = '74678628';

update mutant_fast_search
                                set mfs_genox_zdb_id = 'ZDB-GENOX-180522-1'
                              where mfs_genox_zdb_id = 'ZDB-GENOX-180221-7';

delete from fish_experiment where genox_zdb_id = 'ZDB-GENOX-180221-7';

update fish_experiment
                                set genox_fish_zdb_id = 'ZDB-FISH-180522-1'
                              where genox_fish_zdb_id = 'ZDB-FISH-150901-28051';

update record_attribution set recattrib_data_zdb_id = 'ZDB-FISH-180522-1' where recattrib_data_zdb_id = 'ZDB-FISH-150901-28051';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-FISH-150901-28051';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-FISH-180522-1' where zrepld_new_zdb_id = 'ZDB-FISH-150901-28051';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-FISH-150901-28051';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-FISH-150901-28051', 'ZDB-FISH-180522-1');

