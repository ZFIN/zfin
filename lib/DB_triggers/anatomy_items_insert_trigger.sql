--CREATE ANATOMY_ITEM TRIGGERS
----------------------------------------------------------
--Check that the stage hours are logical: that the stage_start_hours are > 
--the end_stage_hours.    
--REPLACES:
--sub anatomyItemStageWindowConsistent in validatedata.pl

create trigger anatomy_items_insert_trigger 
  insert on anatomy_item
  referencing new as new_stage
  for each row (
      execute procedure p_stg_hours_consistent
  	(new_stage.anatitem_start_stg_zdb_id,new_stage.anatitem_end_stg_zdb_id)
      );
