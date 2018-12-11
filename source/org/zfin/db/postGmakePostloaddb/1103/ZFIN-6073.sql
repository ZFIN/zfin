--liquibase formatted sql
--changeset xshao:ZFIN-6073

delete from zdb_active_data
 where exists(select 1 from db_link
               where dblink_zdb_id = zactvd_zdb_id
                 and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-171018-1' 
                 and dblink_acc_num != dblink_linked_recid);

