create trigger FX_expression_pattern_image_insert_trigger 
  insert on FX_expression_pattern_image
  referencing new as new_xpatfimg
  for each row (
	execute procedure p_check_figure_image_pair_exists (
		new_xpatfimg.xpatfimg_fimgp_zdb_id,
		new_xpatfimg.xpatfimg_fig_zdb_id)
) ;