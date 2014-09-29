SELECT c_gene_id,
       zdb_id,
       organism,
       dblink_zdb_id,
       dblink_linked_recid,
       fdbcont_zdb_id,
       fdbcont_organism_common_name
FROM   orthologue,
       db_link,
       foreign_db_contains
WHERE  dblink_linked_recid = zdb_id
       AND dblink_fdbcont_zdb_id = fdbcont_zdb_id
       AND organism <> fdbcont_organism_common_name;