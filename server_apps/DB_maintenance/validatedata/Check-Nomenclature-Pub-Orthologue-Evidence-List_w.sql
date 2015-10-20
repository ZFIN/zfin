SELECT oev_ortho_zdb_id
       ortho_gene_zdb_id,
       ortho_ncbi_gene_id
       oev_evidence_code
FROM   ortholog,
       ortholog_evidence
WHERE  (oev_source_zdb_id = 'ZDB-PUB-030508-1'
        or oev_source_zdb_id='ZDB-PUB-150115-23' or 
        oev_source_zdb_id='ZDB-PUB-150115-24')
AND  oev_ortho_zdb_id=ortho_zdb_id;


