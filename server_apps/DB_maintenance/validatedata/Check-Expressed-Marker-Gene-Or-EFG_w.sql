SELECT xpatex_source_zdb_id,
       xpatex_gene_zdb_id,
       mrkr_abbrev
FROM   expression_experiment2,
       marker,
       marker_type_group_member
WHERE  xpatex_gene_zdb_id = mrkr_zdb_id
       AND NOT EXISTS (SELECT 't'
                       FROM   marker_type_group_member
                       WHERE  mtgrpmem_mrkr_type_group = 'GENEDOM_AND_EFG'
                              AND mtgrpmem_mrkr_type = mrkr_type);