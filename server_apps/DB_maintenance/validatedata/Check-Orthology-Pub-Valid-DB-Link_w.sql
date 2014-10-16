SELECT recattrib_data_zdb_id,
       dblink_linked_recid,
       dblink_acc_num
FROM   record_attribution,
       db_link
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
       AND dblink_zdb_id = recattrib_data_zdb_id
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