select tscript_mrkr_zdb_id
  from transcript, marker_relationship
  where tscript_mrkr_Zdb_id = mrel_mrkr_2_zdb_id
 and not exists (Select 'x' from db_link 
     	 		where mrel_mrkr_1_zdb_id = dblink_linked_recid)
 and tscript_status_id = 1
 and mrel_mrkr_1_zdb_id like 'ZDB-GENE%'
 ;