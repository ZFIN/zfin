--liquibase formatted sql
--changeset sierra:drop_weird_mrph.sql

insert into withdrawn_data (wd_old_zdb_id, wd_new_zdb_id)
 values ('ZDB-MRPHLNO-041116-4','ZDB-PUB-030508-1');

delete from zdb_active_data
 where zactvd_zdb_id = 'ZDB-MRPHLNO-041116-4';

