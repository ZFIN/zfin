--CREATE FISH_IMAGE_STAGE TRIGGERS
-----------------------------------------------------------
--check that the stage hours are logical: that the stage_start_hours are > 
--the end_stage_hours.
--REPLACES:
--sub fishImageAnatomyStageWindowOverlapsAnatomyItem in validatedata.pl

create trigger fish_image_stage_update_trigger 
  update of fimgstg_start_stg_zdb_id,fimgstg_end_stg_zdb_id 
  on fish_image_stage 
  referencing new as new_stage
  for each row (
      execute procedure p_stg_hours_consistent (
        new_stage.fimgstg_start_stg_zdb_id, 
  	new_stage.fimgstg_end_stg_zdb_id
      )
  );