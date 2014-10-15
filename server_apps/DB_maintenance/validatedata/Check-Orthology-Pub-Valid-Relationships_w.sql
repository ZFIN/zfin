SELECT recattrib_data_zdb_id,
       mrel_mrkr_1_zdb_id,
       mrel_mrkr_2_zdb_id,
       mrel_type
FROM   record_attribution,
       marker_relationship
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
       AND mrel_zdb_id = recattrib_data_zdb_id
       AND recattrib_data_zdb_id NOT IN (SELECT oevdisp_gene_zdb_id
                                         FROM   orthologue_evidence_display
                                         WHERE  EXISTS
                                         (SELECT recattrib_data_zdb_id
                                          FROM   record_attribution
                                          WHERE  recattrib_source_zdb_id
                                                 =
                                                 'ZDB-PUB-030905-1'
                                                 AND oevdisp_gene_zdb_id
                                                     =
                                                     recattrib_data_zdb_id)
                                                AND EXISTS (SELECT
                                                    recattrib_data_zdb_id
                                                            FROM
                                                    record_attribution
                                                            WHERE
                                                    recattrib_source_zdb_id
                                                    = 'ZDB-PUB-030905-1'
                                                    AND oevdisp_zdb_id =
           recattrib_data_zdb_id)
                                         UNION
                                         SELECT oevdisp_zdb_id
                                         FROM   orthologue_evidence_display
                                         WHERE  EXISTS
                                         (SELECT recattrib_data_zdb_id
                                          FROM   record_attribution
                                          WHERE  recattrib_source_zdb_id
                                                 =
                                                 'ZDB-PUB-030905-1'
                                                 AND oevdisp_gene_zdb_id
                                                     =
                                                     recattrib_data_zdb_id)
                                                AND EXISTS (SELECT
                                                    recattrib_data_zdb_id
                                                            FROM
                                                    record_attribution
                                                            WHERE
                                                    recattrib_source_zdb_id
                                                    = 'ZDB-PUB-030905-1'
                                                    AND oevdisp_zdb_id =
           recattrib_data_zdb_id));