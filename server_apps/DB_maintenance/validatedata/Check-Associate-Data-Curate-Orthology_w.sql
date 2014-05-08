SELECT recattrib_data_zdb_id,
  oevdisp_gene_zdb_id,
  mrkr_abbrev
FROM   record_attribution,
  orthologue_evidence_display,
  marker
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030905-1'
       AND Get_obj_type(recattrib_data_zdb_id) NOT IN (
  'DBLINK', 'MREL', 'MRKRGOEV', 'DALIAS', 'ORTHO' )
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
                                                              AND oevdisp_gene_zdb_id
                                                                  =
                                                                  recattrib_data_zdb_id
                                         )
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
                                                                  recattrib_data_zdb_id))
       AND oevdisp_zdb_id = recattrib_data_zdb_id
       AND mrkr_zdb_id = oevdisp_gene_zdb_id  