--CREATE ANATOMY_ITEM TRIGGERS
----------------------------------------------------------
--Check that the stage hours are logical: that the stage_start_hours are > 
--the end_stage_hours.    
--REPLACES:
--sub anatomyItemStageWindowConsistent in validatedata.pl

create trigger anatomy_items_update_trigger 
  update of anatitem_start_stg_zdb_id, anatitem_end_stg_zdb_id on anatomy_item
    referencing old as old_stage new as new_stage
    for each row (
        execute procedure p_stg_hours_consistent
  	  (new_stage.anatitem_start_stg_zdb_id, 
           new_stage.anatitem_end_stg_zdb_id)
    );
