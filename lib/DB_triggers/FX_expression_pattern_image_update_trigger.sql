create trigger FX_expression_pattern_image_update_trigger
   update of xpatfimg_fimgp_zdb_id, 
	     xpatfimg_fig_zdb_id, 
 	     xpatfimg_panel_designation
   on fx_expression_pattern_image
   referencing new as new_xpatfimg
   for each row 
	(
	execute procedure p_check_figure_image_pair_exists(
		new_xpatfimg.xpatfimg_fimgp_zdb_id,
                new_xpatfimg.xpatfimg_fig_zdb_id),
	execute function scrub_char(new_xpatfimg.xpatfimg_panel_designation) 
		into xpatfimg_panel_designation
) ;