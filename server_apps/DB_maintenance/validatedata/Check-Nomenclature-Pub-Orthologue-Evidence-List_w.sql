SELECT recattrib_data_zdb_id,
       oevdisp_gene_zdb_id,
       oevdisp_organism_list,
       oevdisp_evidence_code
FROM   record_attribution,
       orthologue_evidence_display
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030508-1'
       AND recattrib_data_zdb_id = oevdisp_zdb_id;