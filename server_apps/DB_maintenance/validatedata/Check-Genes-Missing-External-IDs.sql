SELECT m.mrkr_zdb_id AS zdb_id,
       m.mrkr_abbrev AS gene_symbol,
       m.mrkr_name AS gene_name,
       CASE WHEN NOT EXISTS (
           SELECT 1 FROM db_link WHERE dblink_linked_recid = m.mrkr_zdb_id
             AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
       ) THEN 'MISSING' ELSE '' END AS ncbi_gene_id,
       CASE WHEN NOT EXISTS (
           SELECT 1 FROM db_link WHERE dblink_linked_recid = m.mrkr_zdb_id
             AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-47'
       ) THEN 'MISSING' ELSE '' END AS uniprot,
       CASE WHEN EXISTS (
           SELECT 1 FROM marker_annotation_status
            WHERE mas_mrkr_zdb_id = m.mrkr_zdb_id
              AND mas_vt_pk_id = 13
       ) THEN 'YES' ELSE '' END AS not_in_current_release
FROM marker m
WHERE m.mrkr_type = 'GENE'
  AND (NOT EXISTS (
           SELECT 1 FROM db_link WHERE dblink_linked_recid = m.mrkr_zdb_id
             AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1')
       OR NOT EXISTS (
           SELECT 1 FROM db_link WHERE dblink_linked_recid = m.mrkr_zdb_id
             AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-47')
       OR EXISTS (
           SELECT 1 FROM marker_annotation_status
            WHERE mas_mrkr_zdb_id = m.mrkr_zdb_id
              AND mas_vt_pk_id = 13))
ORDER BY m.mrkr_abbrev;
