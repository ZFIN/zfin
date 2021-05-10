select mrkr_zdb_id, mrkr_name, mrkr_abbrev
               from marker m1
               where mrkr_type = "GENE"
                 and mrkr_name like "xx:%"
                 and exists
                     ( select * 
                         from marker m2, marker_relationship
                         where mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                           and mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id
                           and mrel_type = "gene encodes small segment" 
                           and exists
                               ( select * 
                                   from clone
                                   where clone_mrkr_zdb_id = m2.mrkr_zdb_id ) )
               order by mrkr_name
