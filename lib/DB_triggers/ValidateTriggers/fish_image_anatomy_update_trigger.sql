 create trigger fish_image_anatomy_update_trigger update of
  	fimganat_fimg_start_stg_zdb_id,fimganat_fimg_end_stg_zdb_id 
  on fish_image_anatomy 
  referencing new as new_stage
  for each row 
	(execute procedure p_stg_hours_consistent
  		(new_stage.fimganat_fimg_start_stg_zdb_id, 
  		new_stage.fimganat_fimg_end_stg_zdb_id));