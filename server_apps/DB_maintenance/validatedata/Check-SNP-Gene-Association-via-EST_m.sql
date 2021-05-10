UNLOAD TO genesESTsWithContracdictingSNPs
SELECT distinct mr1.mrel_mrkr_1_zdb_id as gene_id, gene.mrkr_abbrev as gene_symbol, mr1.mrel_mrkr_2_zdb_id as EST_id, est.mrkr_abbrev as EST_symbol
  FROM marker_relationship mr1, marker gene, marker est , marker_type_group_member
 WHERE mr1.mrel_type = 'gene encodes small segment'
   AND get_obj_type(mr1.mrel_mrkr_2_zdb_id) = 'EST'
   AND mr1.mrel_mrkr_1_zdb_id= gene.mrkr_zdb_id
   and gene.mrkr_type=mtgrpmem_mrkr_type
   and mtgrpmem_mrkr_type_group='GENEDOM'
   AND mr1.mrel_mrkr_2_zdb_id= est.mrkr_zdb_id
   AND NOT EXISTS (SELECT 'x' FROM clone c
                    WHERE c.clone_mrkr_zdb_id = mr1.mrel_mrkr_2_zdb_id
                      AND c.clone_problem_type = 'Chimeric')
   AND EXISTS (SELECT 'x' FROM marker_relationship mr2
                WHERE mr2.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_2_zdb_id
                  AND mr2.mrel_type = 'contains polymorphism'
                  AND get_obj_type(mr2.mrel_mrkr_2_zdb_id) = 'SNP'
                  AND NOT EXISTS (SELECT 'x' FROM marker_relationship mr3
                                   WHERE mr3.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_1_zdb_id
                                     AND mr3.mrel_type = 'contains polymorphism'
                                     AND get_obj_type(mr3.mrel_mrkr_2_zdb_id) = 'SNP'
                                 )
              )
UNION
SELECT distinct mr1.mrel_mrkr_1_zdb_id as gene_id, gene.mrkr_abbrev as gene_symbol, mr1.mrel_mrkr_2_zdb_id as EST_id, est.mrkr_abbrev as EST_symbol
  FROM marker_relationship mr1, marker gene, marker est, marker_type_group_member
 WHERE mr1.mrel_type = 'gene encodes small segment'
   AND mr1.mrel_mrkr_1_zdb_id= gene.mrkr_zdb_id
   and gene.mrkr_type=mtgrpmem_mrkr_type
   and mtgrpmem_mrkr_type_group='GENEDOM'
   AND get_obj_type(mr1.mrel_mrkr_2_zdb_id) = 'EST'
   AND mr1.mrel_mrkr_1_zdb_id= gene.mrkr_zdb_id
   AND mr1.mrel_mrkr_2_zdb_id= est.mrkr_zdb_id
   AND NOT EXISTS (SELECT 'x' FROM clone c
                    WHERE c.clone_mrkr_zdb_id = mr1.mrel_mrkr_2_zdb_id
                      AND c.clone_problem_type = 'Chimeric')
   AND EXISTS (SELECT 'x' FROM marker_relationship mr2
                WHERE mr2.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_2_zdb_id
                  AND mr2.mrel_type = 'contains polymorphism'
                  AND get_obj_type(mr2.mrel_mrkr_2_zdb_id) = 'SNP'
                  AND NOT EXISTS (SELECT 'x' FROM marker_relationship mr3
                                   WHERE mr3.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_1_zdb_id
                                     AND mr3.mrel_type = 'contains polymorphism'
                                     AND get_obj_type(mr3.mrel_mrkr_2_zdb_id) = 'SNP'
                                     AND mr3.mrel_mrkr_2_zdb_id = mr2.mrel_mrkr_2_zdb_id
                                 )
              )   
UNION
SELECT distinct mr1.mrel_mrkr_1_zdb_id as gene_id, gene.mrkr_abbrev as gene_symbol, mr1.mrel_mrkr_2_zdb_id as EST_id, est.mrkr_abbrev as EST_symbol
  FROM marker_relationship mr1, marker gene, marker est, marker_type_group_member
 WHERE  mr1.mrel_type = 'gene encodes small segment'
   AND mr1.mrel_mrkr_1_zdb_id= gene.mrkr_zdb_id
   and gene.mrkr_type=mtgrpmem_mrkr_type
   and mtgrpmem_mrkr_type_group='GENEDOM'
   AND get_obj_type(mr1.mrel_mrkr_2_zdb_id) = 'EST'
   AND mr1.mrel_mrkr_1_zdb_id= gene.mrkr_zdb_id
   AND mr1.mrel_mrkr_2_zdb_id= est.mrkr_zdb_id
   AND NOT EXISTS (SELECT 'x' FROM clone c
                    WHERE c.clone_mrkr_zdb_id = mr1.mrel_mrkr_2_zdb_id
                      AND c.clone_problem_type = 'Chimeric')
   AND EXISTS (SELECT 'x' FROM marker_relationship mr2
                WHERE mr2.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_2_zdb_id
                  AND mr2.mrel_type = 'contains polymorphism'
                  AND get_obj_type(mr2.mrel_mrkr_2_zdb_id) = 'SNP'
                  AND EXISTS (SELECT 'x' FROM marker_relationship mr3
                               WHERE mr3.mrel_mrkr_2_zdb_id = mr2.mrel_mrkr_2_zdb_id
                                 AND mr3.mrel_type = 'contains polymorphism'
                                 AND (get_obj_type(mr3.mrel_mrkr_1_zdb_id) = 'GENE' OR mr3.mrel_mrkr_1_zdb_id like '%RNAG%')
                                 AND mr3.mrel_mrkr_1_zdb_id != mr1.mrel_mrkr_1_zdb_id
                             )
              )
ORDER BY gene_symbol, EST_symbol            
;   

UNLOAD TO genesWithSNPsBelongToESTnotAssociated
SELECT distinct mr1.mrel_mrkr_1_zdb_id as gene_id, gene.mrkr_abbrev as gene_symbol, mr1.mrel_mrkr_2_zdb_id as SNP_id, snp.mrkr_abbrev as SNP_symbol
  FROM marker_relationship mr1, marker gene, marker snp, marker_type_group_member
 WHERE  mr1.mrel_type = 'contains polymorphism'
  AND mr1.mrel_mrkr_1_zdb_id= gene.mrkr_zdb_id
   and gene.mrkr_type=mtgrpmem_mrkr_type
   and mtgrpmem_mrkr_type_group='GENEDOM'
   AND get_obj_type(mr1.mrel_mrkr_2_zdb_id) = 'SNP'
   AND mr1.mrel_mrkr_1_zdb_id= gene.mrkr_zdb_id
   AND mr1.mrel_mrkr_2_zdb_id= snp.mrkr_zdb_id
   AND EXISTS (SELECT 'x' FROM marker_relationship mr2
                WHERE get_obj_type(mr2.mrel_mrkr_1_zdb_id) = 'EST'
                  AND mr2.mrel_type = 'contains polymorphism'
                  AND get_obj_type(mr2.mrel_mrkr_2_zdb_id) = 'SNP'
                  AND mr2.mrel_mrkr_2_zdb_id = mr1.mrel_mrkr_2_zdb_id
                  AND NOT EXISTS (SELECT 'x' FROM marker_relationship mr3
                                   WHERE mr3.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_1_zdb_id
                                     AND mr3.mrel_type = 'gene encodes small segment'
                                     AND (get_obj_type(mr3.mrel_mrkr_1_zdb_id) = 'GENE' OR mr3.mrel_mrkr_1_zdb_id like '%RNAG%')
                                     AND get_obj_type(mr3.mrel_mrkr_2_zdb_id) = 'EST'
                                     AND mr3.mrel_mrkr_2_zdb_id = mr2.mrel_mrkr_1_zdb_id
                                 )
              )
ORDER BY gene_symbol, SNP_symbol
;

