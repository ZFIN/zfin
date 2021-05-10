--liquibase formatted sql
--changeset xshao:ZFIN-5869

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-070117-822';

insert into zdb_replaced_data (zrepld_old_zdb_id, zrepld_new_zdb_id)
 values ('ZDB-GENE-070117-822', 'ZDB-ALT-060125-2');

