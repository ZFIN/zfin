select a.dblink_linked_recid
 from db_link a
 where not exists (select 'x' from db_link b, foreign_db_contains, foreign_db_data_type
                        where b.dblink_linked_recid = a.dblink_linked_recid
                        and b.dblink_fdbcont_Zdb_id = fdbcont_zdb_id
                        and fdbcont_fdbdt_id = fdbdt_pk_id
                        and fdbdt_data_type = 'RNA')
and (a.dblink_linked_recid like 'ZDB-GENE%' or a.dblink_linked_recid like '%RNAG%')
and (a.dblink_acc_num like 'BK%' or a.dblink_acc_num like 'BN%')
