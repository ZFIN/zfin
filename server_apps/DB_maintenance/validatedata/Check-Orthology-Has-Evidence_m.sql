SELECT zdb_id,
       c_gene_id,
       organism
FROM   orthologue
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   orthologue_evidence
                   WHERE  zdb_id = oev_ortho_zdb_id);