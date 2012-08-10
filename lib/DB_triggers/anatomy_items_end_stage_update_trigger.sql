-- CREATE ANATOMY_ITEM UPDATE TRIGGER
----------------------------------------------------------
-- The stage window check replaced the anatomyItemStageWindowConsistent 
-- test in validatedata.pl

create trigger anatomy_items_end_stage_update_trigger 
  update of anatitem_end_stg_zdb_id on anatomy_item
    referencing new as new_anatomy_item
    for each row (
		execute procedure p_stg_hours_consistent
  	  		(new_anatomy_item.anatitem_start_stg_zdb_id, 
           		 new_anatomy_item.anatitem_end_stg_zdb_id),
		execute procedure p_check_anat_anatrel_stg_consistent
			(new_anatomy_item.anatitem_zdb_id, 
			 new_anatomy_item.anatitem_start_stg_zdb_id,
			 new_anatomy_item.anatitem_end_stg_zdb_id)
    );
