  create trigger expression_pattern_image_update_trigger update of 
  	xpatfimg_xpat_start_stg_zdb_id, xpatfimg_xpat_end_stg_zdb_id 
  on expression_pattern_image
  referencing new as new_xpatfimg
  for each row (
    execute procedure p_fimg_overlaps_stg_window(
	new_xpatfimg.xpatfimg_fimg_zdb_id, 
  	new_xpatfimg.xpatfimg_xpat_start_stg_zdb_id, 
  	new_xpatfimg.xpatfimg_xpat_end_stg_zdb_id)
  );
