create trigger expression_pattern_anatomy_update_trigger update of  
  	xpatanat_xpat_start_stg_zdb_id, xpatanat_xpat_end_stg_zdb_id 
  on expression_pattern_anatomy
  referencing new as new_stage 
  for each row
	(execute procedure p_stg_hours_consistent
  		(new_stage.xpatanat_xpat_start_stg_zdb_id, 
  		new_stage.xpatanat_xpat_end_stg_zdb_id));
