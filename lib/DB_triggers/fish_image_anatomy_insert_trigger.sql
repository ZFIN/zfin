--CREATE FISH_IMAGE_ANATOMY TRIGGERS
-------------------------------------------------
-- Check that the stage window in the fish_image_anatomy record overlaps
-- with the stage window for anatomy item in fish_image_anatomy.
--REPLACES:
--sub fishImageAnatomyStageWindowOverlapsAnatomyItem

create trigger fish_image_anatomy_insert_trigger 
  insert on fish_image_anatomy
  referencing new as new_fimganat
  for each row(
      execute procedure p_check_anatitem_overlaps_stg_window (
        new_fimganat.fimganat_anat_item_zdb_id,
        new_fimganat.fimganat_fimg_start_stg_zdb_id, 
  	new_fimganat.fimganat_fimg_end_stg_zdb_id
      )
  );
