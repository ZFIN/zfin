SELECT ortho_zdb_id,
       ortho_gene_zdb_id,
       ortho_ncbi_gene_id
FROM   orthologe
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   orthologue_evidence
                   WHERE  ortho_zdb_id = oev_ortho_zdb_id);
