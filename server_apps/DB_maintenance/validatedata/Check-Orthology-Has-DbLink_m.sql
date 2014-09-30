SELECT zdb_id,
       c_gene_id,
       ortho_name
FROM   orthologue
WHERE  zdb_id NOT IN (SELECT dblink_linked_recid
                      FROM   db_link);