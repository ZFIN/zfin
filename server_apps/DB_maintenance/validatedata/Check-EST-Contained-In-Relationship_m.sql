select m1.mrkr_zdb_id, m1.mrkr_name, m1.mrkr_abbrev
               from marker m1, marker_type_group_member
               where m1.mrkr_type = mtgrpmem_mrkr_type
               and mtgrpmem_mrkr_type_group='GENEDOM'
                 and 1 <>
                     ( select count(*) 
                         from marker m2, marker_relationship
                         where mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                           and mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id
                           and mrel_type = "gene encodes small segment" )
               order by mrkr_name
