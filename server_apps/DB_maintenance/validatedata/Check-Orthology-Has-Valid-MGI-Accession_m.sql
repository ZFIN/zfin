SELECT zdb_id,
       c_gene_id,
       organism
FROM   orthologue
WHERE  organism = 'Mouse'
       AND zdb_id IN (SELECT dblink_linked_recid
                      FROM   db_link,
                             foreign_db_contains,
                             foreign_db
                      WHERE  dblink_fdbcont_zdb_id = fdbcont_zdb_id
                             AND fdbcont_organism_common_name = 'Mouse'
                             AND fdbcont_fdb_db_id = fdb_db_pk_id
                             AND fdb_db_name = 'MGI'
                             AND dblink_acc_num LIKE 'MGI:%');