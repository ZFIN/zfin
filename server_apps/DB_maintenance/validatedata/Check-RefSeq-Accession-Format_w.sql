select dblink_linked_recid, 'RefSeq', dblink_acc_num
                 from db_link, foreign_db_contains, foreign_db_data_type,foreign_db
                where fdbcont_zdb_id = dblink_fdbcont_zdb_id
                  and fdb_db_name = 'RefSeq'
                  and fdbdt_super_type = 'sequence'
                  and fdbcont_fdbdt_id = fdbdt_pk_id
                  and fdbcont_fdb_db_id = fdb_db_pk_id
                  and substring(dblink_acc_num,3,1) <> '_'
               UNION
               select dblink_linked_recid, fdb_db_name, dblink_acc_num
                 from db_link, foreign_db_contains, foreign_db, foreign_db_data_type
                where fdbcont_zdb_id = dblink_fdbcont_zdb_id
                  and fdb_db_name <> 'RefSeq'
                  and fdbdt_super_type = 'sequence'
                  and fdbcont_fdbdt_id = fdbdt_pk_id
                  and fdbcont_fdb_db_id = fdb_db_pk_id
                  and substring(dblink_acc_num,3,1) = '_';
