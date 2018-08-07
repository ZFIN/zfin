--liquibase formatted sql
--changeset xshao:CUR-826

delete from record_attribution where recattrib_pk_id = '33891081';

update record_attribution set recattrib_data_zdb_id = 'ZDB-FISH-180803-1' where recattrib_data_zdb_id = 'ZDB-FISH-150901-18089';

delete from zdb_replaced_data where zrepld_old_zdb_id = 'ZDB-FISH-150901-18089';

update zdb_replaced_data set zrepld_new_zdb_id = 'ZDB-FISH-180803-1' where zrepld_new_zdb_id = 'ZDB-FISH-150901-18089';

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-FISH-150901-18089';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id) values ('ZDB-FISH-150901-18089', 'ZDB-FISH-180803-1');

