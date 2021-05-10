SELECT a.cc_construct_zdb_id, 
       a.cc_component, 
       mrkr_name, 
       recattrib_source_zdb_id 
FROM   construct_component a, 
       construct_component b, 
       controlled_vocabulary, 
       construct, 
       record_attribution, 
       marker 
WHERE  a.cc_construct_zdb_id = b.cc_construct_zdb_id 
       AND EXISTS (SELECT 'x' 
                   FROM   marker, 
                          marker_type_group_member 
                   WHERE  mrkr_type = mtgrpmem_mrkr_type 
                          AND mtgrpmem_mrkr_type_group = 'CONSTRUCT_COMPONENTS' 
                          AND mrkr_zdb_id = a.cc_component_zdb_id) 
       AND b.cc_component_zdb_id = cv_zdb_id 
       AND b.cc_order + 2 = a.cc_order 
       AND recattrib_data_zdb_id = mrkr_zdb_id 
       AND mrkr_zdb_id = a.cc_construct_zdb_id 
       AND cv_foreign_species IS NOT NULL 
UNION 
SELECT a.cc_construct_zdb_id, 
       a.cc_component, 
       mrkr_name, 
       recattrib_source_zdb_id 
FROM   construct_component a, 
       construct_component b, 
       controlled_vocabulary, 
       construct, 
       record_attribution, 
       marker 
WHERE  a.cc_construct_zdb_id = b.cc_construct_zdb_id 
       AND EXISTS (SELECT 'x' 
                   FROM   marker_type_group_member
                   WHERE  mrkr_type = mtgrpmem_mrkr_type 
                          AND mtgrpmem_mrkr_type_group = 'CONSTRUCT_COMPONENTS' 
                          AND mrkr_zdb_id = a.cc_component_zdb_id) 
       AND recattrib_data_zdb_id = mrkr_zdb_id 
       AND mrkr_zdb_id = a.cc_construct_zdb_id 
       AND b.cc_component_zdb_id = cv_zdb_id 
       AND b.cc_order + 1 = a.cc_order 
       AND cv_foreign_species IS NOT NULL; 