SELECT h.discontinued_gene_id,
       h.discontinued_symbol AS ncbi_old_symbol,
       h.replacement_gene_id,
       h.discontinue_date,
       CASE h.replacement_gene_id WHEN '-' THEN 'Withdrawn' ELSE 'Replaced' END AS status,
       old_link.dblink_linked_recid AS old_zdb_id,
       old_marker.mrkr_abbrev AS old_zfin_symbol,
       new_link.dblink_linked_recid AS new_zdb_id,
       new_marker.mrkr_abbrev AS new_zfin_symbol
FROM tmp_ncbi_gene_history h
LEFT JOIN db_link old_link
       ON old_link.dblink_acc_num = h.discontinued_gene_id
      AND old_link.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
LEFT JOIN marker old_marker
       ON old_marker.mrkr_zdb_id = old_link.dblink_linked_recid
LEFT JOIN db_link new_link
       ON new_link.dblink_acc_num = h.replacement_gene_id
      AND new_link.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
LEFT JOIN marker new_marker
       ON new_marker.mrkr_zdb_id = new_link.dblink_linked_recid
ORDER BY CASE
             WHEN old_link.dblink_zdb_id IS NOT NULL THEN 0
             WHEN new_link.dblink_zdb_id IS NOT NULL THEN 1
             WHEN h.replacement_gene_id = '-' THEN 2
             ELSE 3
         END,
         h.discontinued_gene_id;
