--CREATE FISH_IMAGE_ANATOMY TRIGGERS
-------------------------------------------------
--check that the stage hours are logical: that the stage_start_hours are <
--the end_stage_hours.   
--REPLACES:
--sub fishImageAnatomyStageWindowOverlapsAnatomyItem

create trigger fish_image_anatomy_insert_trigger 
  insert on fish_image_anatomy
  referencing new as new_stage
  for each row(
      execute procedure p_stg_hours_consistent (
        new_stage.fimganat_fimg_start_stg_zdb_id, 
  	new_stage.fimganat_fimg_end_stg_zdb_id
      )
  );
