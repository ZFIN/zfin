SELECT recattrib_data_zdb_id, 
       mrkrgoev_mrkr_zdb_id, 
       mrkrgoev_evidence_code, 
       mrkrgoev_term_zdb_id 
FROM   record_attribution, 
       marker_go_term_evidence 
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-1' 
       AND mrkrgoev_zdb_id = recattrib_data_zdb_id 
       AND recattrib_data_zdb_id NOT IN (SELECT oevdisp_gene_zdb_id 
                                         FROM   orthologue_evidence_display 
                                         WHERE  EXISTS 
                                         (SELECT recattrib_data_zdb_id 
                                          FROM   record_attribution 
                                          WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                                                 AND oevdisp_gene_zdb_id = recattrib_data_zdb_id)
                                                 AND EXISTS (SELECT recattrib_data_zdb_id
                                                             FROM record_attribution
                                                             WHERE recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                                                             AND oevdisp_zdb_id = recattrib_data_zdb_id)
                                         UNION 
                                         SELECT oevdisp_zdb_id 
                                         FROM   orthologue_evidence_display 
                                         WHERE  EXISTS 
                                         (SELECT recattrib_data_zdb_id 
                                          FROM   record_attribution 
                                          WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                                                 AND oevdisp_gene_zdb_id = recattrib_data_zdb_id)
                                                 AND EXISTS (SELECT recattrib_data_zdb_id
                                                             FROM record_attribution
                                                             WHERE recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
                                                             AND oevdisp_zdb_id = recattrib_data_zdb_id));