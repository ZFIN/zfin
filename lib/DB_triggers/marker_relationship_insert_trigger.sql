create trigger marker_relationship_insert_trigger 
  insert on marker_relationship
  referencing new as new_mrkr_rel
     for each row (
         execute procedure p_mrel_grpmem_correct (
	   new_mrkr_rel.mrel_mrkr_1_zdb_id, 
	   new_mrkr_rel.mrel_mrkr_2_zdb_id, 
	   new_mrkr_rel.mrel_type
         ),
	 execute procedure checkTscriptType (new_mrkr_rel.mrel_mrkr_1_zdb_id, 
	 	 	   		     new_mrkr_rel.mrel_mrkr_2_zdb_id,
					     new_mrkr_rel.mrel_type)
     );

