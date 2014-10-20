SELECT recattrib_data_zdb_id,
       oevdisp_gene_zdb_id,
       oevdisp_evidence_code,
       oevdisp_organism_list
FROM   record_attribution r1,
       orthologue_evidence_display
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
       AND recattrib_data_zdb_id = oevdisp_zdb_id
       AND NOT EXISTS (
                      -- all nucleotide accession numbers assoc. w/pub via dblink_zdb_id (DBLINK)
                      SELECT recattrib_data_zdb_id
                      FROM   db_link,
                             record_attribution r2,
                             foreign_db_contains,
                             foreign_db_data_type
                      WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                             AND dblink_zdb_id = recattrib_data_zdb_id
                             AND fdbcont_fdbdt_id = fdbdt_pk_id
                             AND dblink_fdbcont_zdb_id = fdbcont_zdb_id
                             AND fdbdt_data_type IN (
                                 'Genomic', 'RNA', 'Sequence Clusters'
                                                    )
                             AND r1.recattrib_data_zdb_id =
                                 r2.recattrib_data_zdb_id
                       UNION
                       -- all nucleotide accession numbers assoc. w/pub via dblink_linked_recid (GENE)
                       SELECT recattrib_data_zdb_id
                       FROM   db_link,
                              record_attribution r3,
                              foreign_db_contains,
                              foreign_db_data_type
                       WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-2'
                              AND dblink_linked_recid = recattrib_data_zdb_id
                              AND dblink_fdbcont_zdb_id = fdbcont_zdb_id
                              AND fdbcont_fdbdt_id = fdbdt_pk_id
                              AND fdbdt_data_type IN (
                                  'Genomic', 'RNA', 'Sequence Clusters' )
                              AND r1.recattrib_data_zdb_id =
                                  r3.recattrib_data_zdb_id)
ORDER  BY recattrib_data_zdb_id;