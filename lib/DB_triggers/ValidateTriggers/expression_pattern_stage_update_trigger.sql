 create trigger expression_pattern_stage_update_trigger update of 
	xpatstg_start_stg_zdb_id, xpatstg_end_stg_zdb_id 
  on expression_pattern_stage
  referencing new as new_stage 
  for each row 
	(execute procedure p_stg_hours_consistent
  		(new_stage.xpatstg_start_stg_zdb_id, 
		new_stage.xpatstg_end_stg_zdb_id));
