--CREATE ANATOMY_CONTAINS TRIGGERS
-----------------------------------------------------
--check that the stage hours are logical: that the stage_start_hours are > 
--the end_stage_hours.  
--REPLACES: 
--sub anatomyContainsStageWindowConsistent in validatedata.pl

create trigger anatomy_contains_update_trigger 
  update of anatcon_start_stg_zdb_id, anatcon_end_stg_zdb_id on anatomy_contains  
  referencing new as new_stage
  for each row (
      execute procedure p_stg_hours_consistent (new_stage.anatcon_start_stg_zdb_id,
              new_stage.anatcon_end_stg_zdb_id)
      );