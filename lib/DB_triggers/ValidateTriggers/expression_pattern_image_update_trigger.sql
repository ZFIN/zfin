  create trigger expression_pattern_image_update_trigger update of 
  	xpatfimg_xpat_start_stg_zdb_id, xpatfimg_xpat_end_stg_zdb_id 
  on expression_pattern_image
  referencing new as new_stage
  for each row (execute procedure p_stg_hours_consistent
  	(new_stage.xpatfimg_xpat_start_stg_zdb_id, 
  	new_stage.xpatfimg_xpat_end_stg_zdb_id));