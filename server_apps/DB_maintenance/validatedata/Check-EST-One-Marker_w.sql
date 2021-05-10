SELECT mrkr_zdb_id, 
       mrkr_name, 
       mrkr_abbrev 
FROM   marker m2 
WHERE  mrkr_type = 'EST'
       AND 1 < (SELECT count(*)
                FROM   marker m1, 
                       marker_relationship 
                WHERE  mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id 
                       AND mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id 
                       AND m1.mrkr_type IN (SELECT mtgrpmem_mrkr_type 
                                            FROM   marker_type_group_member 
                                            WHERE  mtgrpmem_mrkr_type_group = 
                                                   'GENEDOM'
                                           ) 
                       AND mrel_type = 'gene encodes small segment') 
ORDER  BY mrkr_name; 
