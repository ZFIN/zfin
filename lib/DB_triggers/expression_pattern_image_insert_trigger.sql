create trigger expression_pattern_image_insert_trigger 
  insert on expression_pattern_image
  referencing new as new_xpatfimg
  for each row (
    execute procedure p_fimg_overlaps_stg_window(
	new_xpatfimg.xpatfimg_fimg_zdb_id, 
  	new_xpatfimg.xpatfimg_xpat_start_stg_zdb_id, 
  	new_xpatfimg.xpatfimg_xpat_end_stg_zdb_id)
  );
