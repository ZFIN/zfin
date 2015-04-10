SELECT distinct mr1.mrel_mrkr_1_zdb_id as gene_id, mr1.mrel_mrkr_2_zdb_id as EST_id
FROM   marker_relationship mr1 
WHERE  get_obj_type(mr1.mrel_mrkr_1_zdb_id) = "GENE" 
      AND mr1.mrel_type = "gene encodes small segment" 
      AND get_obj_type(mr1.mrel_mrkr_2_zdb_id) = "EST"
      AND NOT EXISTS (SELECT "x" FROM clone c
                       WHERE c.clone_mrkr_zdb_id = mr1.mrel_mrkr_2_zdb_id
                         AND c.clone_problem_type = "Chimeric")
      AND EXISTS (SELECT "x" FROM marker_relationship mr2
                   WHERE mr2.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_2_zdb_id
                     AND mr2.mrel_type = "contains polymorphism"
                     AND get_obj_type(mr2.mrel_mrkr_2_zdb_id) = "SNP"
                     AND NOT EXISTS (SELECT "x" FROM marker_relationship mr3
                                      WHERE mr3.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_1_zdb_id
                                        AND mr3.mrel_type = "contains polymorphism"
                                        AND get_obj_type(mr3.mrel_mrkr_2_zdb_id) = "SNP"
                                    )
                 )
UNION
SELECT distinct mr1.mrel_mrkr_1_zdb_id as gene_id, mr1.mrel_mrkr_2_zdb_id as EST_id
  FROM marker_relationship mr1 
 WHERE get_obj_type(mr1.mrel_mrkr_1_zdb_id) = "GENE" 
   AND mr1.mrel_type = "gene encodes small segment" 
   AND get_obj_type(mr1.mrel_mrkr_2_zdb_id) = "EST"
   AND NOT EXISTS (SELECT "x" FROM clone c
                    WHERE c.clone_mrkr_zdb_id = mr1.mrel_mrkr_2_zdb_id
                      AND c.clone_problem_type = "Chimeric")
   AND EXISTS (SELECT "x" FROM marker_relationship mr2
                WHERE mr2.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_2_zdb_id
                  AND mr2.mrel_type = "contains polymorphism"
                  AND get_obj_type(mr2.mrel_mrkr_2_zdb_id) = "SNP"
                  AND NOT EXISTS (SELECT "x" FROM marker_relationship mr3
                                   WHERE mr3.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_1_zdb_id
                                     AND mr3.mrel_type = "contains polymorphism"
                                     AND get_obj_type(mr3.mrel_mrkr_2_zdb_id) = "SNP"
                                     AND mr3.mrel_mrkr_2_zdb_id = mr2.mrel_mrkr_2_zdb_id
                                 )
              )   
UNION
SELECT distinct mr1.mrel_mrkr_1_zdb_id as gene_id, mr1.mrel_mrkr_2_zdb_id as EST_id
FROM   marker_relationship mr1 
WHERE  get_obj_type(mr1.mrel_mrkr_1_zdb_id) = "GENE" 
      AND mr1.mrel_type = "gene encodes small segment" 
      AND get_obj_type(mr1.mrel_mrkr_2_zdb_id) = "EST"
      AND NOT EXISTS (SELECT "x" FROM clone c
                       WHERE c.clone_mrkr_zdb_id = mr1.mrel_mrkr_2_zdb_id
                         AND c.clone_problem_type = "Chimeric")
      AND EXISTS (SELECT "x" FROM marker_relationship mr2
                   WHERE mr2.mrel_mrkr_1_zdb_id = mr1.mrel_mrkr_2_zdb_id
                     AND mr2.mrel_type = "contains polymorphism"
                     AND get_obj_type(mr2.mrel_mrkr_2_zdb_id) = "SNP"
                     AND EXISTS (SELECT "x" FROM marker_relationship mr3
                                  WHERE mr3.mrel_mrkr_2_zdb_id = mr2.mrel_mrkr_2_zdb_id
                                    AND mr3.mrel_type = "contains polymorphism"
                                    AND get_obj_type(mr3.mrel_mrkr_1_zdb_id) = "GENE"
                                    AND mr3.mrel_mrkr_1_zdb_id != mr1.mrel_mrkr_1_zdb_id
                                )
                 )
;  

