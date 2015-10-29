SELECT o.ortho_zdb_id,
       o.ortho_zebrafish_gene_zdb_id,
       og.organism_common_name
FROM   ortholog o,organism og
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   ortholog_evidence
                   WHERE  ortho_zdb_id = oev_ortho_zdb_id)
AND o.ortho_other_species_taxid=og.organism_taxid;
