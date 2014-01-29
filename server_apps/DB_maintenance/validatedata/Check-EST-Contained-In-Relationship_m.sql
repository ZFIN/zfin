select mrkr_zdb_id, mrkr_name, mrkr_abbrev
               from marker m1
               where mrkr_type = "GENE"
                 and mrkr_name[3]  == ":"
                 and mrkr_name[1,2] not in ("id","si")
                 and 1 <> 
                     ( select count(*) 
                         from marker m2, marker_relationship
                         where mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                           and mrel_mrkr_2_zdb_id = m2.mrkr_zdb_id
                           and mrel_type = "gene encodes small segment" )
               order by mrkr_name
